import { Component, signal } from '@angular/core';
import { BaseChartDirective } from 'ng2-charts';
import { ChartData, ChartOptions } from 'chart.js';
import { CommonImportsModule } from '../shared/common-imports/common-imports-module';
import { DashboardSummary } from '../shared/types/types';
import { DashboardService } from '../shared/service/dashboard-service';

interface MonthOption {
  label: string;
  value: number;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonImportsModule, BaseChartDirective],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard {
  private readonly now = new Date();

  selectedYear = this.now.getFullYear();
  selectedMonth = this.now.getMonth() + 1;

  yearOptions: number[] = [];
  monthOptions: MonthOption[] = [];
  currentMonth = '';

  loading = false;
  errorMessage = '';

  totals = signal({
    expense: 0,
    income: 0,
    investment: 0,
    transfer: 0,
  });

  expenses = signal<DashboardSummary['expenses']>([]);

  pieData: ChartData<'pie'> = {
    labels: [],
    datasets: [],
  };

  barData: ChartData<'bar'> = {
    labels: ['Income', 'Expense', 'Investment', 'Transfer'],
    datasets: [],
  };

  pieOptions: ChartOptions<'pie'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom',
      },
    },
  };

  transactionBreakdownPieData: ChartData<'pie'> = {
    labels: ['Income', 'Expenses', 'Investments', 'Transfers'],
    datasets: [],
  };

  transactionBreakdownPieOptions: ChartOptions<'pie'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom',
      },
    },
  };

  barOptions: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: false,
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: {
          callback: (value) => `₹${Number(value).toLocaleString('en-IN')}`,
        },
      },
    },
  };

  constructor(private readonly dashboardService: DashboardService) {
    this.yearOptions = Array.from({ length: 10 }, (_, index) => this.now.getFullYear() - index);

    this.generateMonthOptions();
    this.refreshData();
  }

  private generateMonthOptions(): void {
    const maxMonth = this.selectedYear === this.now.getFullYear() ? this.now.getMonth() + 1 : 12;

    this.monthOptions = Array.from({ length: maxMonth }, (_, index) => {
      const month = maxMonth - index;
      const date = new Date(this.selectedYear, month - 1, 1);

      return {
        label: date.toLocaleDateString('en-IN', {
          month: 'long',
        }),
        value: month,
      };
    });

    this.selectedMonth = this.monthOptions[0].value;
    this.updateCurrentMonthLabel();
  }

  onYearChange(year: string | number): void {
    this.selectedYear = Number(year);
    this.generateMonthOptions();
    this.refreshData();
  }

  onMonthChange(month: string | number): void {
    this.selectedMonth = Number(month);
    this.updateCurrentMonthLabel();
    this.refreshData();
  }

  private updateCurrentMonthLabel(): void {
    const date = new Date(this.selectedYear, this.selectedMonth - 1, 1);

    this.currentMonth = date.toLocaleDateString('en-IN', {
      month: 'long',
      year: 'numeric',
    });
  }

  refreshData(): void {
    this.loading = true;
    this.errorMessage = '';

    this.dashboardService.fetchDashboardSummary(this.selectedYear, this.selectedMonth).subscribe({
      next: (data: DashboardSummary) => {
        this.totals.set({
          expense: data.transactions?.expense ?? 0,
          income: data.transactions?.income ?? 0,
          investment: data.transactions?.investment ?? 0,
          transfer: data.transactions?.transfer ?? 0,
        });

        this.expenses.set(data.expenses ?? []);
        this.updateCharts();
        this.loading = false;
      },

      error: (error) => {
        console.error('Failed to fetch dashboard summary', error);

        this.errorMessage = 'Unable to load dashboard data. Please try again.';

        this.totals.set({
          expense: 0,
          income: 0,
          investment: 0,
          transfer: 0,
        });

        this.expenses.set([]);
        this.updateCharts();
        this.loading = false;
      },
    });
  }

  resetToCurrentMonth(): void {
    // Get the current date each time Refresh is clicked.
    const currentDate = new Date();

    this.selectedYear = currentDate.getFullYear();
    this.selectedMonth = currentDate.getMonth() + 1;

    // Rebuild dropdown options and update the displayed month.
    this.generateMonthOptions();

    // Load data for the current year and month.
    this.refreshData();
  }

  // Absolute amount helper for display and calculations.
  abs(amount: number): number {
    return Math.abs(amount ?? 0);
  }

  // Percentage of total income.
  // Returns zero if income is zero or negative.
  getIncomePercentage(amount: number): number {
    const income = this.totals().income;

    if (income <= 0) {
      return 0;
    }

    return (Math.abs(amount) / income) * 100;
  }

  // Keep progress bars within the valid 0-100% range.
  getIncomeBarPercentage(amount: number): number {
    return Math.min(100, Math.max(0, this.getIncomePercentage(amount)));
  }

  // Income remaining after expenses, investments and transfers.
  getRemainingIncome(): number {
    const totals = this.totals();

    return (
      totals.income -
      Math.abs(totals.expense) -
      Math.abs(totals.investment) -
      Math.abs(totals.transfer)
    );
  }

  // Remaining income as a percentage of income.
  getRemainingPercentage(): number {
    const income = this.totals().income;

    if (income <= 0) {
      return 0;
    }

    return (this.getRemainingIncome() / income) * 100;
  }

  // Remaining balance progress bar, capped between 0 and 100.
  getRemainingBarPercentage(): number {
    return Math.min(100, Math.max(0, this.getRemainingPercentage()));
  }

  // Combined percentage of expenses, investments and transfers.
  getTotalAllocatedPercentage(): number {
    const totals = this.totals();

    return this.getIncomePercentage(
      Math.abs(totals.expense) + Math.abs(totals.investment) + Math.abs(totals.transfer),
    );
  }

  private updateCharts(): void {
    // Expense breakdown by category.
    const sortedExpenses = [...this.expenses()]
      .map((item) => ({
        ...item,
        amount: Math.abs(item.amount ?? 0),
      }))
      .sort((a, b) => b.amount - a.amount);

    this.pieData = {
      labels: sortedExpenses.map((item) => item.categoryName),
      datasets: [
        {
          data: sortedExpenses.map((item) => item.amount),
          backgroundColor: [
            '#6366f1',
            '#f59e0b',
            '#10b981',
            '#ec4899',
            '#06b6d4',
            '#8b5cf6',
            '#f97316',
            '#14b8a6',
          ],
          borderWidth: 2,
          borderColor: '#ffffff',
        },
      ],
    };

    // Monthly transaction breakdown.
    this.transactionBreakdownPieData = {
      labels: ['Income', 'Expenses', 'Investments', 'Transfers'],
      datasets: [
        {
          data: [
            this.totals().income,
            Math.abs(this.totals().expense),
            Math.abs(this.totals().investment),
            Math.abs(this.totals().transfer),
          ],
          backgroundColor: ['#16a34a', '#ef4444', '#8b5cf6', '#0ea5e9'],
          borderWidth: 2,
          borderColor: '#ffffff',
        },
      ],
    };

    // Monthly cash flow.
    this.barData = {
      labels: ['Income', 'Expense', 'Investment', 'Transfer'],
      datasets: [
        {
          label: 'Amount',
          data: [
            this.totals().income,
            Math.abs(this.totals().expense),
            Math.abs(this.totals().investment),
            Math.abs(this.totals().transfer),
          ],
          backgroundColor: ['#16a34a', '#ef4444', '#8b5cf6', '#0ea5e9'],
          borderRadius: 8,
          maxBarThickness: 55,
        },
      ],
    };
  }
}
