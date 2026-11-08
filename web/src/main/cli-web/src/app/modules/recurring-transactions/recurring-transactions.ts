import { Component, inject, ViewChild } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { DataGrid } from '../shared/data-grid/data-grid';
import { CommonService } from '../shared/service/common-service';
import { NotificationService } from '../shared/service/notification-service';
import { RecurringTransactionRule, RecurringTransactionsService } from '../shared/service/recurring-transactions-service';
import { DATA_FIELDS, GRID_EXPORT_FILE_NAMES, GRID_NAMES, MODES } from '../shared/enums';
import { ApiResponse, GridColumn, ToolbarConfig } from '../shared/types/types';
import { AddEditRecurringTransactionDialog } from './add-edit-recurring-transaction-dialog/add-edit-recurring-transaction-dialog';
import { ConfirmationDialog } from '../shared/confirmation-dialog/confirmation-dialog';
import { MatDialog } from '@angular/material/dialog';
import { MessageService } from '../shared/service/message-service';
import { CommonImportsModule } from '../shared/common-imports/common-imports-module';

@Component({
  selector: 'app-recurring-transactions',
  imports: [DataGrid, CommonImportsModule],
  templateUrl: './recurring-transactions.html',
})
export class RecurringTransactions {
  @ViewChild('dataGrid') private dataGrid!: DataGrid;
  private readonly recurringService = inject(RecurringTransactionsService);
  private readonly notificationService = inject(NotificationService);
  private readonly dialog = inject(MatDialog);
  private readonly messageService = inject(MessageService);
  protected readonly commonService = inject(CommonService);

  protected readonly gridName = GRID_NAMES.RECURRING_TRANSACTIONS_GRID;
  protected readonly gridExportFileName = GRID_EXPORT_FILE_NAMES.RECURRING_TRANSACTIONS_GRID_EFN;
  protected readonly dataKey = DATA_FIELDS.ID;
  protected readonly toolbarConfig: ToolbarConfig;
  constructor() {
    this.toolbarConfig = this.commonService.toolbarConfig(
      true,
      'Settings',
      'Recurring Transactions',
    );
    this.toolbarConfig.copyRow = false;
    this.toolbarConfig.refresh = true;
  }

  calculateCellValue = (row: any, column: GridColumn): string => {
    if (column.field === 'frequency') {
      const units: Record<string, string> = {
        MINUTELY: 'minutes',
        HOURLY: 'hours',
        DAILY: 'days',
        WEEKLY: 'weeks',
        MONTHLY: 'months',
        YEARLY: 'years',
      };
      const unit = units[row.frequency] ?? String(row.frequency).toLowerCase();
      const interval = `Every ${row.intervalValue} ${Number(row.intervalValue) === 1 ? unit.replace(/s$/, '') : unit}`;
      if (row.frequency === 'WEEKLY') {
        return `${interval} on ${['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'][row.dayOfWeek - 1]}`;
      }
      if (row.frequency === 'MONTHLY') {
        if (row.monthlyPattern === 'LAST_DAY') return `${interval} on the last day`;
        if (row.monthlyPattern === 'LAST_WORKING_DAY') return `${interval} on the last working day`;
        return `${interval} on day ${row.dayOfMonth}`;
      }
      if (row.frequency === 'YEARLY') return `${interval} on ${row.monthOfYear}/${row.dayOfMonth}`;
      return interval;
    }
    if (column.field === 'active') return row.active ? 'Active' : 'Inactive';
    return String(row[column.field] ?? '');
  };

  addRow = (): void => this.openRuleDialog(MODES.ADD);

  editRow = (): void => {
    if (this.dataGrid?.selectedRows.length === 0) {
      this.notificationService.error(this.messageService.get('common.edit.noSelection'));
      return;
    }
    if (this.dataGrid?.selectedRows.length > 1) {
      this.notificationService.error(this.messageService.get('common.edit.singleSelection'));
      return;
    }
    const selectedRow = this.dataGrid.selectedRows[0];
    if (!this.canManageRule(selectedRow)) {
      this.notificationService.error(this.messageService.get('recurringTransaction.ownership.editDenied'));
      return;
    }
    this.openRuleDialog(MODES.EDIT, selectedRow);
  };

  deleteRow = (): void => this.confirmSelectedAction('delete');
  activate = (): void => this.updateSelectedRules(true, 'activate');
  inactivate = (): void => this.updateSelectedRules(false, 'inactivate');

  private openRuleDialog(mode: string, selectedRow?: RecurringTransactionRule): void {
    this.dialog
      .open(AddEditRecurringTransactionDialog, {
        width: '80vw',
        maxWidth: '900px',
        data: { mode, selectedRow },
      })
      .afterClosed()
      .subscribe((message) => {
        if (message) {
          this.notificationService.success(message);
          this.dataGrid?.refreshGrid();
        }
      });
  }

  private updateSelectedRules(active: boolean, action: 'activate' | 'inactivate'): void {
    const rows = this.dataGrid?.selectedRows ?? [];
    if (!rows.length) {
      this.notificationService.error(this.messageService.get(`common.${action}.noSelection`));
      return;
    }

    const manageableRows = rows.filter((row) => this.canManageRule(row));
    if (!manageableRows.length) {
      this.notificationService.error(this.messageService.get(`recurringTransaction.ownership.${action}Denied`));
      return;
    }
    const statusRows = manageableRows.filter((row) => row.active !== active);
    if (!statusRows.length) {
      const alreadyMessage = this.messageService.get(
        `recurringTransaction.${action}.already${active ? 'Active' : 'Inactive'}`,
      );
      const message = manageableRows.length !== rows.length
        ? `${this.messageService.get('recurringTransaction.ownership.mixedSelection')} ${alreadyMessage}`
        : alreadyMessage;
      this.notificationService.error(message);
      return;
    }

    const hasMixedOwnership = manageableRows.length !== rows.length;
    const hasMixedStatus = statusRows.length !== manageableRows.length;
    const confirmation = this.dialog.open(ConfirmationDialog, {
      width: 'auto',
      data: {
        title: this.messageService.get(`common.${action}.title`),
        message: [
          hasMixedOwnership ? this.messageService.get('recurringTransaction.ownership.mixedSelection') : '',
          hasMixedStatus
            ? this.messageService.get(`recurringTransaction.${action}.mixedSelection`)
            : this.messageService.get(`recurringTransaction.${action}.confirmation`),
        ].filter(Boolean).join(' '),
        isNotification: false,
      },
    });
    confirmation.afterClosed().subscribe((confirmed: boolean) => {
      if (!confirmed) return;
      firstValueFrom(this.recurringService.setRulesActive(
        statusRows.map((row) => row.id),
        active,
      )).then((response: ApiResponse) => {
        if (response.success) {
          this.notificationService.success(response.message);
          this.dataGrid?.refreshGrid();
        } else {
          this.notificationService.error(response.message);
        }
      });
    });
  }

  private confirmSelectedAction(action: 'delete'): void {
    const rows = this.dataGrid?.selectedRows ?? [];
    if (!rows.length) {
      this.notificationService.error(this.messageService.get(`common.${action}.noSelection`));
      return;
    }
    const manageableRows = rows.filter((row) => this.canManageRule(row));
    if (!manageableRows.length) {
      this.notificationService.error(this.messageService.get('recurringTransaction.ownership.deleteDenied'));
      return;
    }
    const hasMixedOwnership = manageableRows.length !== rows.length;
    const confirmation = this.dialog.open(ConfirmationDialog, {
      width: 'auto',
      data: {
        title: this.messageService.get('common.delete.title'),
        message: `${hasMixedOwnership ? `${this.messageService.get('recurringTransaction.ownership.mixedSelection')} ` : ''}Are you sure you want to delete ${manageableRows.length} selected recurring transaction rule(s)?`,
        isNotification: false,
      },
    });
    confirmation.afterClosed().subscribe((confirmed: boolean) => {
      if (!confirmed) return;
      const ids = manageableRows.map((row) => row.id);
      firstValueFrom(this.recurringService.deleteRules(ids)).then((response: ApiResponse) => {
        if (response.success) {
          this.notificationService.success(response.message);
          this.dataGrid?.refreshGrid();
        } else {
          this.notificationService.error(response.message);
        }
      });
    });
  }

  private canManageRule(row: any): boolean {
    const profile = this.commonService.getUserProfile;
    return profile.id != null
      && (profile.id === profile.adminUserId || row.createdById === profile.id);
  }
}
