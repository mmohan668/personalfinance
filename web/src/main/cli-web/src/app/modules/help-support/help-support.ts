import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';

interface HelpAction {
  title: string;
  icon: string;
  description: string;
  details?: string;
}

interface HelpSection {
  title: string;
  description: string;
  actions: HelpAction[];
}

interface GridGuide {
  title: string;
  icon: string;
  description: string;
  overview: string;
  features: HelpAction[];
  tips: string[];
}

@Component({
  selector: 'app-help-support',
  standalone: true,
  imports: [
    CommonModule,
    MatIconModule,
    MatButtonModule,
    MatCardModule,
    MatDividerModule,
    MatExpansionModule,
  ],
  templateUrl: './help-support.html',
  styleUrl: './help-support.scss',
})
export class HelpSupport {
  protected readonly supportEmail = 'personalfinanceapp.support@gmail.com';

  readonly helpSections: HelpSection[] = [
    {
      title: 'Record Management',
      description: 'Create, view, update, and manage records.',
      actions: [
        {
          title: 'Refresh',
          icon: 'refresh',
          description: 'Refreshes the data displayed in the grid.',
          details: 'Use this button to retrieve the latest records and update the grid.',
        },
        {
          title: 'Add',
          icon: 'add',
          description: 'Creates a new record.',
          details:
            'Click Add to open the form used to create a record. Enter the required information and save it.',
        },
        {
          title: 'Copy',
          icon: 'file_copy',
          description: 'Creates a copy of an existing record.',
          details:
            'Select the record you want to copy, then click Copy. Review the copied information, update the necessary fields, and save the new record.',
        },
        {
          title: 'Edit',
          icon: 'edit',
          description: 'Modifies an existing record.',
          details:
            'Select the record you want to update and click Edit. Make the necessary changes and save them.',
        },
        {
          title: 'Delete',
          icon: 'delete',
          description: 'Deletes an existing record.',
          details:
            'Select the record you want to remove and click Delete. Verify the selected record before confirming deletion, if a confirmation prompt is provided.',
        },
        {
          title: 'Activate',
          icon: 'toggle_on',
          description: 'Changes a record to an active state.',
          details:
            'Select an eligible inactive record and click Activate to change its status to active.',
        },
        {
          title: 'Inactivate',
          icon: 'toggle_off',
          description: 'Changes a record to an inactive state.',
          details:
            'Select an eligible active record and click Inactivate to change its status to inactive.',
        },
      ],
    },
    {
      title: 'Grid Settings',
      description: 'Save or restore your preferred grid configuration.',
      actions: [
        {
          title: 'Grid Settings',
          icon: 'save',
          description: 'Opens the grid settings menu.',
          details:
            'Click this button to access the options for saving or resetting your grid configuration.',
        },
        {
          title: 'Save Grid Settings',
          icon: 'save',
          description: 'Saves your current grid configuration.',
          details:
            'Save your preferred grid setup, including supported layout and column visibility preferences.',
        },
        {
          title: 'Reset Grid Settings',
          icon: 'restart_alt',
          description: 'Restores the default grid configuration.',
          details:
            'Use this option to return to the default grid layout instead of your customized settings.',
        },
      ],
    },
    {
      title: 'Export Data',
      description: 'Export records for reporting and further analysis.',
      actions: [
        {
          title: 'Export',
          icon: 'ios_share',
          description: 'Opens the data export menu.',
          details: 'Choose whether to export all available records or only the selected rows.',
        },
        {
          title: 'Export All Data',
          icon: 'file_download',
          description: 'Exports all available records to an Excel file.',
          details: 'Choose this option when you need a complete export of the available data.',
        },
        {
          title: 'Export Selected Rows Data',
          icon: 'checklist',
          description: 'Exports only the selected records to an Excel file.',
          details:
            'Select one or more rows in the grid, open the Export menu, and choose this option.',
        },
      ],
    },
    {
      title: 'Column Chooser',
      description: 'Customize which columns appear in the grid.',
      actions: [
        {
          title: 'Column Chooser',
          icon: 'view_column',
          description: 'Opens the column visibility menu.',
          details:
            'Use this menu to search for columns and control which fields are displayed in the grid.',
        },
        {
          title: 'Search Columns',
          icon: 'search',
          description: 'Finds columns by their displayed names.',
          details:
            'Enter a column name in the search field to filter the list and locate a specific column.',
        },
        {
          title: 'Clear Search',
          icon: 'close',
          description: 'Clears the column search text.',
          details:
            'Click the close icon in the search field to clear the search and display the unfiltered column list.',
        },
        {
          title: 'Select All',
          icon: 'select_all',
          description: 'Selects all columns in the current list.',
          details:
            'Use the checkbox to make all columns in the displayed list visible. If the list is filtered by a search, selection applies to the filtered columns.',
        },
        {
          title: 'Individual Column Checkbox',
          icon: 'check_box',
          description: 'Shows or hides an individual column.',
          details: 'Check a column to display it in the grid. Uncheck it to hide it.',
        },
      ],
    },
  ];

  readonly gridGuides: GridGuide[] = [
    {
      title: 'Dashboard',
      icon: 'dashboard',
      description:
        'View a summary of your financial activity, income allocation, and monthly trends.',
      overview:
        'The Financial Dashboard provides a consolidated overview of expenses, income, investments, and transfers. Use it to review a selected month, understand how income is allocated, and compare financial activity over time.',
      features: [
        {
          title: 'Year Filter',
          icon: 'calendar_today',
          description: 'Select the year to review.',
          details:
            'Choose a year from the Year dropdown. The dashboard reloads its data for the selected year and the month selected by the dashboard logic.',
        },
        {
          title: 'Month Filter',
          icon: 'date_range',
          description: 'Select the month to review.',
          details:
            'Choose a month from the Month dropdown to update the dashboard figures and charts for that reporting period.',
        },
        {
          title: 'Refresh Dashboard',
          icon: 'refresh',
          description: 'Returns the dashboard to the current month and refreshes its data.',
          details:
            'Click Refresh to reset the year and month to the current date and request the latest dashboard summary.',
        },
        {
          title: 'Total Expenses',
          icon: 'trending_up',
          description: 'Displays the total expenses for the selected month.',
          details:
            'Use this summary card to see the expense amount reported for the selected period.',
        },
        {
          title: 'Total Income',
          icon: 'trending_down',
          description: 'Displays the total income for the selected month.',
          details: 'Use this card to review the income amount reported for the selected period.',
        },
        {
          title: 'Investments',
          icon: 'account_balance',
          description: 'Displays the investment total for the selected month.',
          details: 'This card summarizes the investment amount reported for the selected period.',
        },
        {
          title: 'Transfers',
          icon: 'swap_horiz',
          description: 'Displays the transfer total for the selected month.',
          details: 'This card summarizes the transfer amount reported for the selected period.',
        },
        {
          title: 'Expense Breakdown',
          icon: 'pie_chart',
          description: 'Shows expenses grouped by category.',
          details:
            'Use the pie chart to compare the expense amounts associated with different categories. If no expense data is available for the selected month, an empty-state message is displayed.',
        },
        {
          title: 'Monthly Transaction Breakdown',
          icon: 'donut_small',
          description:
            'Compares income, expenses, investments, and transfers for the selected month.',
          details:
            'The pie chart displays the relative amounts for the four transaction types. When no transaction amounts are available, the chart displays an empty-state message.',
        },
        {
          title: 'Monthly Cash Flow',
          icon: 'bar_chart',
          description: 'Compares the amounts of income, expenses, investments, and transfers.',
          details:
            'Use the bar chart to compare the four transaction types for the selected reporting period.',
        },
        {
          title: 'Income Utilization',
          icon: 'account_balance_wallet',
          description:
            'Shows how income is distributed across expenses, investments, transfers, and remaining income.',
          details:
            'Each utilization card displays an amount, a percentage, and a progress bar. The section also displays total income and the total allocated percentage. When income is zero or negative, the percentage calculation returns zero.',
        },
        {
          title: 'Last 12 Months Overview',
          icon: 'show_chart',
          description: 'Displays monthly trends for income, expenses, investments, and transfers.',
          details:
            'Use the line chart to compare these transaction types across the most recent 12 months. Hover over chart points to view the corresponding values.',
        },
        {
          title: 'Error Recovery',
          icon: 'error_outline',
          description: 'Provides a retry option when dashboard data cannot be loaded.',
          details:
            'If loading fails, the dashboard displays an error message. Click Try again to request the dashboard data again.',
        },
      ],
      tips: [
        'Use the Year and Month filters to review a particular reporting period.',
        'Click Refresh to return to the current month.',
        'Use the summary cards for a quick view of the four transaction totals.',
        'Compare the expense and transaction breakdown charts to understand the distribution of amounts.',
        'Review Income Utilization to see how income is allocated and what remains.',
        'Use the Last 12 Months Overview to compare transaction trends over time.',
        'If an error appears while loading dashboard data, click Try again.',
      ],
    },
    createTransactionGuide(
      'All Transactions',
      'receipt_long',
      'transaction records',
      'all transaction records',
    ),
    createTransactionGuide(
      'Expense',
      'account_balance_wallet',
      'expense records',
      'expense records',
    ),
    createTransactionGuide('Income', 'payments', 'income records', 'income records'),
    createTransactionGuide('Transfer', 'swap_horiz', 'transfer records', 'transfer records'),
    createTransactionGuide(
      'Investment',
      'account_balance',
      'investment records',
      'investment records',
    ),
    createCategoryGuide(),
    createSubcategoryGuide(),
    createSystemConfigGuide(),
    createReferenceObjectsGuide(),
    createReferenceValuesGuide(),
    createBulkUploadGuide(),
    createUserManagementGuide(),
    createDownloadsGuide(),
  ];
}

function createTransactionGuide(
  title: string,
  icon: string,
  recordLabel: string,
  exportLabel: string,
): GridGuide {
  const singular = title === 'All Transactions' ? 'transaction' : title.toLowerCase();
  return {
    title,
    icon,
    description: `Search, review, and manage ${recordLabel}.`,
    overview: `Use the ${title} grid to find and manage ${recordLabel}. Apply the available search criteria, select the appropriate row before using record actions, and export all records or selected rows when needed.`,
    features: [
      {
        title: 'Date Range',
        icon: 'date_range',
        description: 'Narrow the records by date.',
        details:
          'Choose a suitable date range before searching to reduce the number of records displayed.',
      },
      {
        title: 'Category and Subcategory Filters',
        icon: 'filter_alt',
        description: 'Refine the records using category criteria.',
        details: 'Combine category and subcategory filters when available to narrow the results.',
      },
      {
        title: 'Add',
        icon: 'add',
        description: `Create a new ${singular} record.`,
        details: `Click Add, enter the required information for the ${singular}, and save the record.`,
      },
      {
        title: 'Copy',
        icon: 'file_copy',
        description: `Create another ${singular} from an existing record.`,
        details: `Select the correct ${singular} record before clicking Copy. Review and update the copied information before saving the new record.`,
      },
      {
        title: 'Edit',
        icon: 'edit',
        description: `Update an existing ${singular} record.`,
        details: `Select exactly one ${singular} record before clicking Edit. Review your changes before saving.`,
      },
      {
        title: 'Delete',
        icon: 'delete',
        description: `Delete selected ${singular} records.`,
        details: `Verify the selected ${singular} record or records before confirming deletion.`,
      },
      {
        title: 'Export',
        icon: 'file_download',
        description: `Export ${exportLabel}.`,
        details:
          'Use the export options to download all available records or only the selected rows.',
      },
      {
        title: 'Refresh',
        icon: 'refresh',
        description: 'Request the latest grid data.',
        details: 'Click Refresh to request the latest data for the grid.',
      },
    ],
    tips: [
      'Choose a suitable date range to reduce the number of records displayed.',
      'Combine category and subcategory filters to refine your search when those filters are available.',
      `Use Add to create a new ${singular} record.`,
      `Select the correct ${singular} record before using Copy to create another record.`,
      `Select the correct ${singular} record before using Edit, and review your changes before saving.`,
      `Verify the selected ${singular} record before using Delete.`,
      'Use the export options to download all available records or only selected rows.',
      'Use Refresh to request the latest grid data.',
    ],
  };
}

function createCategoryGuide(): GridGuide {
  return {
    title: 'Categories',
    icon: 'category',
    description: 'Search and manage transaction categories and their active status.',
    overview:
      'Use the Categories grid to find categories by transaction type, category name, and active status. You can add, edit, delete, activate, or inactivate categories. Copy is not available in this grid.',
    features: [
      {
        title: 'Transaction Type Filter',
        icon: 'swap_horiz',
        description: 'Filter categories by transaction type.',
        details: 'Enter a transaction type in the search criteria to narrow the category results.',
      },
      {
        title: 'Category Name Filter',
        icon: 'search',
        description: 'Find categories by name.',
        details: 'Enter a category name to narrow the results.',
      },
      {
        title: 'Active Status Filter',
        icon: 'toggle_on',
        description: 'Filter by All, Active, or Inactive.',
        details:
          'Choose the required status in Active Status. Active and Inactive are displayed as readable status values in the grid.',
      },
      {
        title: 'Search and Clear',
        icon: 'filter_alt',
        description: 'Apply or reset the search criteria.',
        details:
          'Click Search to apply the entered criteria. Click Clear to reset the search fields and grid filters.',
      },
      {
        title: 'Add',
        icon: 'add',
        description: 'Create a category.',
        details: 'Click Add to open the category form, enter the required details, and save.',
      },
      {
        title: 'Edit',
        icon: 'edit',
        description: 'Update one category.',
        details:
          'Select exactly one category before clicking Edit. Make the required changes and save.',
      },
      {
        title: 'Delete',
        icon: 'delete',
        description: 'Delete selected categories.',
        details:
          'Select the category or categories, review the selection, and confirm the deletion.',
      },
      {
        title: 'Activate',
        icon: 'toggle_on',
        description: 'Activate selected categories.',
        details:
          'Select categories that are not already all active, then choose Activate. When prompted, choose whether subcategories should also be activated.',
      },
      {
        title: 'Inactivate',
        icon: 'toggle_off',
        description: 'Inactivate selected categories.',
        details: 'Select categories that are not already all inactive, then confirm Inactivate.',
      },
      {
        title: 'Export',
        icon: 'file_download',
        description: 'Export category records.',
        details:
          'Use the export options to download all available category records or only the selected rows.',
      },
      {
        title: 'Refresh',
        icon: 'refresh',
        description: 'Request the latest category data.',
        details: 'Click Refresh to request the latest data for the Categories grid.',
      },
    ],
    tips: [
      'Use Transaction Type, Category Name, and Active Status together to narrow the category list.',
      'Click Search after changing the search criteria.',
      'Click Clear to reset the search criteria and grid filters.',
      'Select exactly one category before using Edit.',
      'Check the selected categories before deleting or changing their active status.',
      'When activating categories, respond to the prompt about whether subcategories should also be activated.',
      'Copy is not available for Categories.',
      'Use the export options to download all available records or only selected rows.',
      'Use Refresh to request the latest grid data.',
    ],
  };
}

function createSubcategoryGuide(): GridGuide {
  return {
    title: 'Subcategories',
    icon: 'account_tree',
    description: 'Search and manage subcategories and their active status.',
    overview:
      'Use the Subcategories grid to find records by transaction type, category name, subcategory name, and active status. You can add, copy, edit, delete, activate, or inactivate subcategories.',
    features: [
      {
        title: 'Transaction Type Filter',
        icon: 'swap_horiz',
        description: 'Filter subcategories by transaction type.',
        details: 'Enter a transaction type in the search criteria to narrow the results.',
      },
      {
        title: 'Category Name Filter',
        icon: 'category',
        description: 'Find subcategories associated with a category name.',
        details: 'Enter a category name to narrow the subcategory results.',
      },
      {
        title: 'Subcategory Name Filter',
        icon: 'search',
        description: 'Find subcategories by name.',
        details: 'Enter a subcategory name to narrow the results.',
      },
      {
        title: 'Active Status Filter',
        icon: 'toggle_on',
        description: 'Filter by All, Active, or Inactive.',
        details:
          'Choose the required status in Active Status. The grid displays status as Active or Inactive.',
      },
      {
        title: 'Search and Clear',
        icon: 'filter_alt',
        description: 'Apply or reset the search criteria.',
        details:
          'Click Search to apply the entered criteria. Click Clear to reset the search fields and grid filters.',
      },
      {
        title: 'Add',
        icon: 'add',
        description: 'Create a subcategory.',
        details: 'Click Add to open the subcategory form, enter the required details, and save.',
      },
      {
        title: 'Copy',
        icon: 'file_copy',
        description: 'Create a subcategory based on an existing one.',
        details:
          'Select exactly one subcategory before clicking Copy. Review the copied details, make any necessary changes, and save the new subcategory.',
      },
      {
        title: 'Edit',
        icon: 'edit',
        description: 'Update one subcategory.',
        details:
          'Select exactly one subcategory before clicking Edit. Review your changes before saving.',
      },
      {
        title: 'Delete',
        icon: 'delete',
        description: 'Delete selected subcategories.',
        details:
          'Select the subcategory or subcategories, review the selection, and confirm the deletion.',
      },
      {
        title: 'Activate',
        icon: 'toggle_on',
        description: 'Activate selected subcategories.',
        details: 'Select subcategories that are not already all active, then confirm Activate.',
      },
      {
        title: 'Inactivate',
        icon: 'toggle_off',
        description: 'Inactivate selected subcategories.',
        details: 'Select subcategories that are not already all inactive, then confirm Inactivate.',
      },
      {
        title: 'Export',
        icon: 'file_download',
        description: 'Export subcategory records.',
        details:
          'Use the export options to download all available subcategory records or only the selected rows.',
      },
      {
        title: 'Refresh',
        icon: 'refresh',
        description: 'Request the latest subcategory data.',
        details: 'Click Refresh to request the latest data for the Subcategories grid.',
      },
    ],
    tips: [
      'Use Transaction Type, Category Name, Subcategory Name, and Active Status together to narrow the list.',
      'Click Search after changing the search criteria.',
      'Click Clear to reset the search criteria and grid filters.',
      'Select exactly one subcategory before using Copy or Edit.',
      'Review the copied subcategory before saving it as a new record.',
      'Check the selected subcategories before deleting or changing their active status.',
      'Use the export options to download all available records or only selected rows.',
      'Use Refresh to request the latest grid data.',
    ],
  };
}
function createSystemConfigGuide(): GridGuide {
  return {
    title: 'System Configuration',
    icon: 'tune',
    description: 'Review and configure system-wide preferences, such as currency and date format.',
    overview:
      'Use System Configuration to review system settings, including the currency used to display amounts and the configured date pattern.',
    features: [
      {
        title: 'Review Configuration Settings',
        icon: 'settings',
        description: 'View system configuration names and current values.',
        details:
          'Review the configuration names and their current values to understand which system-wide preferences are in effect.',
      },
      {
        title: 'Currency',
        icon: 'currency_exchange',
        description: 'Change the currency used for amounts.',
        details:
          'Click the Currency configuration value to open the currency update dialog. Choose the desired currency and complete the update.',
      },
      {
        title: 'Date Pattern',
        icon: 'calendar_today',
        description: 'Review or change the configured date pattern.',
        details:
          'Select the date-pattern configuration and use its available setting action to choose the preferred date format.',
      },
      {
        title: 'Export',
        icon: 'file_download',
        description: 'Export system configuration records.',
        details:
          'Use the grid export options to download all available configuration records or only the selected rows.',
      },
      {
        title: 'Refresh',
        icon: 'refresh',
        description: 'Request the latest configuration data.',
        details: 'Click Refresh to request the latest data after a setting has been updated.',
      },
    ],
    tips: [
      'Review the current configuration values before changing a setting.',
      'Click the Currency value to open its update dialog.',
      'Check the displayed currency after updating it.',
      'Review the configured date pattern.',
      'Use Export and Refresh as separate grid actions when needed.',
    ],
  };
}

function createReferenceObjectsGuide(): GridGuide {
  return {
    title: 'Reference Objects',
    icon: 'data_object',
    description: 'View and search application-provided reference objects.',
    overview:
      'Reference Objects contains predefined values supplied by the application. Users can search and view these values, but cannot add, edit, or delete them.',
    features: [
      {
        title: 'Search Reference Objects',
        icon: 'search',
        description: 'Find reference objects by name.',
        details:
          'Enter a reference object name in the Reference Object Name field and click Search to filter the records. Use Clear to reset the search criteria and filters.',
      },
      {
        title: 'View Reference Objects',
        icon: 'visibility',
        description: 'Review the available predefined reference objects.',
        details:
          'Use the grid to view the reference objects provided by the application. These values are managed by the application and cannot be modified by users.',
      },
      {
        title: 'Export',
        icon: 'file_download',
        description: 'Export reference object records.',
        details: 'Use the grid export options to export reference object data when needed.',
      },
      {
        title: 'Refresh',
        icon: 'refresh',
        description: 'Refresh the reference object grid.',
        details: 'Use Refresh to reload the grid data.',
      },
      {
        title: 'Request a New Reference Object',
        icon: 'support_agent',
        description: 'Contact the system administrator to request additional values.',
        details:
          'Users cannot add, edit, or delete reference objects. To request a new reference object, email personalfinanceapp.support@gmail.com with the details of the required value.',
      },
    ],
    tips: [
      'Use Search to find a reference object by name.',
      'Use Clear to reset the search criteria.',
      'Reference objects are predefined and cannot be modified by users.',
      'Contact personalfinanceapp.support@gmail.com to request a new reference object.',
    ],
  };
}

function createReferenceValuesGuide(): GridGuide {
  return {
    title: 'Reference Values',
    icon: 'list_alt',
    description: 'Create and manage reference values used throughout the application.',
    overview:
      'Use Reference Values to maintain the values available for selection in the application. You can add, view, copy, edit, delete, activate, and inactivate reference values as needed.',
    features: [
      {
        title: 'Search Filters',
        icon: 'search',
        description: 'Find reference values using search criteria.',
        details:
          'Use Reference Object Name, Reference Code, and Active Status to narrow the results. Choose All, Active, or Inactive as needed, then click Search. Click Clear to reset the search criteria and grid filters.',
      },
      {
        title: 'Add',
        icon: 'add',
        description: 'Create a new reference value.',
        details:
          'Click Add to open the reference value form. Enter the required information and save the new value for use in the application.',
      },
      {
        title: 'Copy',
        icon: 'file_copy',
        description: 'Create a reference value based on an existing one.',
        details:
          'Select exactly one reference value and click Copy. Review the copied information, make the necessary changes, and save it as a new value.',
      },
      {
        title: 'Edit',
        icon: 'edit',
        description: 'Update an existing reference value.',
        details:
          'Select exactly one reference value and click Edit. Make the required changes and save them.',
      },
      {
        title: 'Delete',
        icon: 'delete',
        description: 'Remove selected reference values.',
        details:
          'Select the reference value or values you want to remove and click Delete. Review the selection and confirm the deletion.',
      },
      {
        title: 'Activate',
        icon: 'toggle_on',
        description: 'Make reference values active.',
        details:
          'Select reference values that are not already all active and click Activate. Confirm the action to activate the selected values.',
      },
      {
        title: 'Inactivate',
        icon: 'toggle_off',
        description: 'Make reference values inactive.',
        details:
          'Select reference values that are not already all inactive and click Inactivate. Confirm the action to inactivate the selected values.',
      },
      {
        title: 'Export',
        icon: 'file_download',
        description: 'Export reference value records.',
        details:
          'Use the grid export options to download reference value data, including all available records or selected rows where supported.',
      },
      {
        title: 'Refresh',
        icon: 'refresh',
        description: 'Reload reference value data.',
        details: 'Click Refresh to request the latest data and update the Reference Values grid.',
      },
    ],
    tips: [
      'Use the search filters together to find specific reference values.',
      'Click Search after changing the search criteria.',
      'Click Clear to reset the search criteria and grid filters.',
      'Select exactly one record before using Copy or Edit.',
      'Review your selection before deleting records or changing their active status.',
      'Inactive reference values may not be available for selection in other parts of the application.',
      'Use Export and Refresh as separate grid actions when needed.',
    ],
  };
}

function createBulkUploadGuide(): GridGuide {
  return {
    title: 'Bulk Upload',
    icon: 'upload_file',
    description: 'Upload multiple records using Excel templates and track upload status.',
    overview:
      'Use Bulk Upload to import financial transactions, categories, subcategories, and reference values from an Excel (.xlsx) file. Download the appropriate template, fill it with your data, and submit it for processing. Track upload status and access uploaded files or error files from the grid.',
    features: [
      {
        title: 'Select Upload Type',
        icon: 'category',
        description: 'Choose the type of data you want to upload.',
        details:
          'Select Financial Transactions, Categories, Subcategories, or Reference Values from the Upload Type list before downloading a template or submitting a file.',
      },
      {
        title: 'Download Template',
        icon: 'download',
        description: 'Download the Excel template for the selected data type.',
        details:
          'Select an upload type and click Download Template. Use the downloaded .xlsx template to prepare your data in the expected format.',
      },
      {
        title: 'Choose File',
        icon: 'attach_file',
        description: 'Select a prepared Excel file for upload.',
        details:
          'Click Choose File and select the completed Excel file. Only .xlsx files are accepted. The selected file name appears on the upload form.',
      },
      {
        title: 'Submit Upload',
        icon: 'cloud_upload',
        description: 'Submit the selected file for processing.',
        details:
          'Select an upload type and an Excel file, then click Submit. On successful submission, the form resets, a confirmation message appears, and the upload status grid refreshes.',
      },
      {
        title: 'Reset Upload Form',
        icon: 'restart_alt',
        description: 'Clear the current upload selections.',
        details:
          'Click Reset to clear the selected upload type, selected file, and file validation error.',
      },
      {
        title: 'Track Upload Status',
        icon: 'fact_check',
        description: 'Review upload records and their processing status.',
        details:
          'Use the grid to review upload status records and monitor submitted uploads. The grid can also provide access to the uploaded file and any associated error file when available.',
      },
      {
        title: 'Download Uploaded File',
        icon: 'description',
        description: 'Retrieve a file associated with an upload record.',
        details:
          'When an uploaded file is available in the grid, use its file link to download it.',
      },
      {
        title: 'Download Error File',
        icon: 'error',
        description: 'Review errors associated with an upload.',
        details:
          'When an error file is available, download it from the grid to inspect the reported issues and correct the data before trying again.',
      },
      {
        title: 'Export',
        icon: 'file_download',
        description: 'Export upload status records.',
        details: 'Use the grid export options to export upload status data when needed.',
      },
      {
        title: 'Refresh',
        icon: 'refresh',
        description: 'Reload the upload status grid.',
        details: 'Click Refresh to retrieve the latest upload status records.',
      },
    ],
    tips: [
      'Download the template that matches the data type you want to upload.',
      'Keep the file in .xlsx format.',
      'Prepare your data using the downloaded template.',
      'Select both an upload type and a file before clicking Submit.',
      'Check the upload status grid after submitting a file.',
      'Download and review the error file when one is available.',
      'Export and Refresh are separate grid actions.',
    ],
  };
}

function createUserManagementGuide(): GridGuide {
  return {
    title: 'User Management',
    icon: 'manage_accounts',
    description: 'Manage users who can access and use the application.',
    overview:
      'The user who signs up for the application becomes the administrator. The administrator can add family members or other users to help manage financial transactions, maintain their accounts, and assign specific permissions based on the actions they are allowed to perform.',
    features: [
      {
        title: 'View Users',
        icon: 'people',
        description: 'Review users associated with your account.',
        details:
          'Use the User Management grid to view user records and review their active status and administrator designation.',
      },
      {
        title: 'Add User',
        icon: 'person_add',
        description: 'Add a family member or another user.',
        details:
          'Click Add to open the user form. Enter the required user information and save the record to add the user to your application.',
      },
      {
        title: 'Edit User',
        icon: 'edit',
        description: 'Update an existing user’s information.',
        details:
          'Select exactly one user and click Edit to update their information. The administrator account cannot be edited through this action.',
      },
      {
        title: 'Delete User',
        icon: 'delete',
        description: 'Remove users from user management.',
        details:
          'Select the user or users you want to delete and click Delete. Review the confirmation prompt before proceeding. The administrator account cannot be deleted through this action.',
      },
      {
        title: 'Activate User',
        icon: 'toggle_on',
        description: 'Activate inactive user accounts.',
        details:
          'Select the inactive users you want to activate and click Activate. Confirm the action to update their status.',
      },
      {
        title: 'Inactivate User',
        icon: 'toggle_off',
        description: 'Inactivate user accounts when access is no longer needed.',
        details:
          'Select the user or users you want to inactivate and click Inactivate. Confirm the action to update their status. The administrator account cannot be inactivated through this action.',
      },
      {
        title: 'Manage Permissions',
        icon: 'admin_panel_settings',
        description: 'Assign specific permissions to individual users.',
        details:
          'The administrator can provide users with specific permissions to control which actions they are allowed to perform within the application.',
      },
      {
        title: 'Export',
        icon: 'file_download',
        description: 'Export user records.',
        details: 'Use the grid export options to export user data when needed.',
      },
      {
        title: 'Refresh',
        icon: 'refresh',
        description: 'Reload the user list.',
        details: 'Click Refresh to retrieve the latest user records and reflect recent changes.',
      },
    ],
    tips: [
      'The user who signs up for the application becomes the administrator.',
      'Add family members or other users who need to manage financial transactions.',
      'Assign permissions according to the actions each user should be allowed to perform.',
      'Review a user’s details before editing or deleting their account.',
      'Use Activate and Inactivate to manage user account status.',
      'The administrator account cannot be edited or inactivated through the corresponding grid actions, and cannot be deleted.',
      'Export and Refresh are separate grid actions.',
    ],
  };
}

function createDownloadsGuide(): GridGuide {
  return {
    title: 'Downloads',
    icon: 'download',
    description: 'View and download files generated by asynchronous grid exports.',
    overview:
      'Grid exports with record counts exceeding the configured limit can be processed asynchronously on the server. Instead of waiting for a large export to finish in the browser, you can continue working and use the Downloads screen to retrieve the generated file once processing is complete.',
    features: [
      {
        title: 'View Exported Files',
        icon: 'folder_open',
        description: 'Review files generated by server-side grid exports.',
        details:
          'The Downloads grid displays exported files grouped by grid name. Review the available file records to find the export you need.',
      },
      {
        title: 'Download File',
        icon: 'file_download',
        description: 'Download an exported file to your device.',
        details:
          'When a file is available for download, use its File Name action to download it through your browser. Files that are not yet available cannot be downloaded from this action.',
      },
      {
        title: 'Refresh',
        icon: 'refresh',
        description: 'Reload the list of available downloads.',
        details:
          'Click Refresh to retrieve the latest download records and check whether a newly generated export is available.',
      },
    ],
    tips: [
      'Smaller exports can be downloaded directly through the browser, while exports exceeding the configured record limit may be processed asynchronously on the server.',
      'Large exports may take additional time to complete.',
      'Return to Downloads and use Refresh to check for newly generated files.',
      'Use the File Name action to download a file when it is available.',
    ],
  };
}
