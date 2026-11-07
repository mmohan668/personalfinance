import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatTabsModule } from '@angular/material/tabs';
import { firstValueFrom } from 'rxjs';
import { CommonImportsModule } from '../../shared/common-imports/common-imports-module';
import { NotificationService } from '../../shared/service/notification-service';
import { UserService } from '../../shared/service/user-service';
import { UserPermissionUpdateRequest, UserPermissionViewDto } from '../../shared/types/types';

interface PermissionScreen {
  id: number;
  name: string;
  permissions: UserPermissionViewDto[];
}

interface PermissionModule {
  id: number;
  name: string;
  screens: PermissionScreen[];
}

interface UserPermissionsDialogData {
  userId: number;
  username: string;
}

@Component({
  imports: [CommonImportsModule, MatButtonModule, MatTabsModule],
  selector: 'app-user-permissions-dialog',
  styleUrl: './user-permissions-dialog.scss',
  templateUrl: './user-permissions-dialog.html',
})
export class UserPermissionsDialog {
  protected readonly dialogRef = inject(MatDialogRef<UserPermissionsDialog>);
  protected readonly data = inject<UserPermissionsDialogData>(MAT_DIALOG_DATA);
  private readonly userService = inject(UserService);
  private readonly notificationService = inject(NotificationService);

  protected permissions: UserPermissionViewDto[] = [];
  protected permissionModules: PermissionModule[] = [];
  protected loading = signal(false);
  protected saving = signal(false);
  private savedPermissions: UserPermissionViewDto[] = [];

  constructor() {
    void this.loadPermissions();
  }

  protected get hasChanges(): boolean {
    const savedById = new Map(
      this.savedPermissions.map((permission) => [permission.id, permission]),
    );
    return this.permissions.some(
      (permission) => savedById.get(permission.id)?.hasPermission !== permission.hasPermission,
    );
  }

  protected areAllSelected(permissions: UserPermissionViewDto[]): boolean {
    return permissions.length > 0 && permissions.every((permission) => permission.hasPermission);
  }

  protected areSomeSelected(permissions: UserPermissionViewDto[]): boolean {
    return (
      permissions.some((permission) => permission.hasPermission) &&
      !this.areAllSelected(permissions)
    );
  }

  protected setPermissions(permissions: UserPermissionViewDto[], selected: boolean): void {
    permissions.forEach((permission) => {
      permission.hasPermission = selected;
    });
  }

  protected modulePermissions(module: PermissionModule): UserPermissionViewDto[] {
    return module.screens.flatMap((screen) => screen.permissions);
  }

  protected setModulePermissions(module: PermissionModule, selected: boolean): void {
    this.setPermissions(this.modulePermissions(module), selected);
  }

  protected reset(): void {
    if (!this.loading() && !this.saving()) {
      void this.loadPermissions();
    }
  }

  protected async update(): Promise<void> {
    if (!this.hasChanges || this.saving()) {
      return;
    }

    const savedById = new Map(
      this.savedPermissions.map((permission) => [permission.id, permission]),
    );
    const request: UserPermissionUpdateRequest = {
      userId: this.data.userId,
      permissions: this.permissions
        .filter(
          (permission) => savedById.get(permission.id)?.hasPermission !== permission.hasPermission,
        )
        .map(({ id, hasPermission }) => ({ id, hasPermission })),
    };

    this.saving.set(true);
    try {
      const response = await firstValueFrom(this.userService.updateUserPermissions(request));
      if (!response.success) {
        this.notificationService.error(response.message);
        return;
      }
      this.savedPermissions = this.permissions.map((permission) => ({ ...permission }));
      this.dialogRef.close(response.message);
    } catch {
      this.notificationService.error('Failed to update user permissions.');
    } finally {
      this.saving.set(false);
    }
  }

  private async loadPermissions(): Promise<void> {
    try {
      this.loading.set(true);
      this.permissions = await firstValueFrom(
        this.userService.fetchUserPermissions(this.data.userId),
      );
      this.savedPermissions = this.permissions.map((permission) => ({ ...permission }));
      this.groupPermissions();
    } catch {
      this.notificationService.error('Failed to load user permissions.');
    } finally {
      this.loading.set(false);
    }
  }

  private groupPermissions(): void {
    const modules = new Map<number, PermissionModule>();
    for (const permission of this.permissions) {
      let module = modules.get(permission.moduleId);
      if (!module) {
        module = {
          id: permission.moduleId,
          name: permission.moduleName,
          screens: [],
        };
        modules.set(permission.moduleId, module);
      }

      let screen = module.screens.find((item) => item.id === permission.screenId);
      if (!screen) {
        screen = {
          id: permission.screenId,
          name: permission.screenName,
          permissions: [],
        };
        module.screens.push(screen);
      }
      screen.permissions.push(permission);
    }
    this.permissionModules = Array.from(modules.values());
  }
}
