import { Component, signal } from '@angular/core';
import { BaseChartDirective } from 'ng2-charts';
import { ChartData, ChartOptions } from 'chart.js';
import { CommonImportsModule } from '../shared/common-imports/common-imports-module';
import { DashboardSummary } from '../shared/types/types';
import { DashboardService } from '../shared/service/dashboard-service';
import { TRANSACTION_TYPES } from '../shared/enums';

interface MonthOption {
  label: string;
  value: number;
}

interface MonthlyTransactionTotalProjection {
  transactionYear: number;
  transactionMonth: number;
  transactionType: string;
  total: number | string;
}

interface MonthlyHistoryPoint {
  label: string;
  year: number;
  month: number;
  income: number;
  expense: number;
  investment: number;
  transfer: number;
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

  last12MonthsData: ChartData<'line'> = {
    labels: [],
    datasets: [
      {
        label: 'Income',
        data: [],
        borderColor: '#16a34a',
        backgroundColor: '#16a34a',
        pointBackgroundColor: '#16a34a',
        pointBorderColor: '#ffffff',
        pointBorderWidth: 2,
        pointRadius: 3,
        pointHoverRadius: 5,
        borderWidth: 3,
        tension: 0.35,
        fill: false,
      },
      {
        label: 'Expenses',
        data: [],
        borderColor: '#ef4444',
        backgroundColor: '#ef4444',
        pointBackgroundColor: '#ef4444',
        pointBorderColor: '#ffffff',
        pointBorderWidth: 2,
        pointRadius: 3,
        pointHoverRadius: 5,
        borderWidth: 3,
        tension: 0.35,
        fill: false,
      },
      {
        label: 'Investments',
        data: [],
        borderColor: '#8b5cf6',
        backgroundColor: '#8b5cf6',
        pointBackgroundColor: '#8b5cf6',
        pointBorderColor: '#ffffff',
        pointBorderWidth: 2,
        pointRadius: 3,
        pointHoverRadius: 5,
        borderWidth: 3,
        tension: 0.35,
        fill: false,
      },
      {
        label: 'Transfers',
        data: [],
        borderColor: '#0ea5e9',
        backgroundColor: '#0ea5e9',
        pointBackgroundColor: '#0ea5e9',
        pointBorderColor: '#ffffff',
        pointBorderWidth: 2,
        pointRadius: 3,
        pointHoverRadius: 5,
        borderWidth: 3,
        tension: 0.35,
        fill: false,
      },
    ],
  };

  last12MonthsOptions: ChartOptions<'line'> = {
    responsive: true,
    maintainAspectRatio: false,
    interaction: {
      mode: 'index',
      intersect: false,
    },
    plugins: {
      legend: {
        display: true,
        position: 'bottom',
        labels: {
          usePointStyle: false,
          boxWidth: 28,
          padding: 18,
        },
      },
      tooltip: {
        callbacks: {
          label: (context) =>
            `${context.dataset.label}: ₹${Number(context.parsed.y ?? 0).toLocaleString('en-IN')}`,
        },
      },
    },
    scales: {
      x: {
        grid: {
          display: false,
        },
        ticks: {
          maxRotation: 0,
          autoSkip: false,
        },
      },
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

        console.log('Full dashboard API response:', data);

        console.log('Monthly projections:', data.monthlyTransactions);

        // Use the monthly projection rows returned by the backend.
        this.updateLast12MonthsChart(
          (data.monthlyTransactions ?? []) as MonthlyTransactionTotalProjection[],
        );

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
        this.updateLast12MonthsChart([]);
        this.updateCharts();
        this.loading = false;
      },
    });
  }

  /**
   * Builds the line chart from the backend's monthly projection rows.
   *
   * The chart displays the last 12 completed calendar months.
   * Example: during November 2026, it displays November 2025
   * through October 2026.
   */

  private updateLast12MonthsChart(projections: MonthlyTransactionTotalProjection[]): void {
    const today = new Date();

    // Include the current month and the previous 11 months.
    // Example: Nov 2025 through Oct 2026.
    const months: MonthlyHistoryPoint[] = [];

    for (let offset = 11; offset >= 0; offset--) {
      const date = new Date(today.getFullYear(), today.getMonth() - offset, 1);

      months.push({
        label: date.toLocaleDateString('en-IN', {
          month: 'short',
          year: '2-digit',
        }),
        year: date.getFullYear(),
        month: date.getMonth() + 1,
        income: 0,
        expense: 0,
        investment: 0,
        transfer: 0,
      });
    }

    const monthMap = new Map<string, MonthlyHistoryPoint>();

    months.forEach((point) => {
      monthMap.set(`${point.year}-${point.month}`, point);
    });

    console.log('Monthly projections received:', projections);

    console.log('Month map keys:', Array.from(monthMap.keys()));

    console.log('Projection row sample:', projections?.[0]);

    for (const projection of projections ?? []) {
      const year = Number(projection.transactionYear);
      const month = Number(projection.transactionMonth);
      const total = Number(projection.total ?? 0);

      if (!Number.isFinite(year) || !Number.isFinite(month) || !Number.isFinite(total)) {
        continue;
      }

      const point = monthMap.get(`${year}-${month}`);

      console.log({
        year,
        month,
        type: projection.transactionType,
        total: projection.total,
        mapKey: `${year}-${month}`,
        matchedMonth: !!point,
      });

      if (!point) {
        continue;
      }

      // Important: normalize to uppercase before matching.
      const type = String(projection.transactionType ?? '')
        .trim()
        .toUpperCase();

      switch (type) {
        case 'INCOME':
          point.income += Math.abs(total);
          break;

        case 'EXPENSE':
        case 'EXPENSES':
          point.expense += Math.abs(total);
          break;

        case 'INVESTMENT':
        case 'INVESTMENTS':
          point.investment += Math.abs(total);
          break;

        case 'TRANSFER':
        case 'TRANSFERS':
          point.transfer += Math.abs(total);
          break;

        default:
          console.warn('Unrecognized transaction type:', type);
      }
    }

    const datasets: ChartData<'line'>['datasets'] = [
      {
        label: 'Income',
        data: months.map((point) => point.income),
        borderColor: '#16a34a',
        backgroundColor: '#16a34a',
        pointBackgroundColor: '#16a34a',
        pointRadius: 4,
        pointHoverRadius: 6,
        borderWidth: 3,
        tension: 0.35,
        fill: false,
      },
      {
        label: 'Expenses',
        data: months.map((point) => point.expense),
        borderColor: '#ef4444',
        backgroundColor: '#ef4444',
        pointBackgroundColor: '#ef4444',
        pointRadius: 4,
        pointHoverRadius: 6,
        borderWidth: 3,
        tension: 0.35,
        fill: false,
      },
      {
        label: 'Investments',
        data: months.map((point) => point.investment),
        borderColor: '#8b5cf6',
        backgroundColor: '#8b5cf6',
        pointBackgroundColor: '#8b5cf6',
        pointRadius: 4,
        pointHoverRadius: 6,
        borderWidth: 3,
        tension: 0.35,
        fill: false,
      },
      {
        label: 'Transfers',
        data: months.map((point) => point.transfer),
        borderColor: '#0ea5e9',
        backgroundColor: '#0ea5e9',
        pointBackgroundColor: '#0ea5e9',
        pointRadius: 4,
        pointHoverRadius: 6,
        borderWidth: 3,
        tension: 0.35,
        fill: false,
      },
    ];

    this.last12MonthsData = {
      labels: months.map((point) => point.label),
      datasets,
    };

    console.log('Line chart data:', this.last12MonthsData);
  }

  resetToCurrentMonth(): void {
    const currentDate = new Date();

    this.selectedYear = currentDate.getFullYear();
    this.selectedMonth = currentDate.getMonth() + 1;

    this.generateMonthOptions();
    this.refreshData();
  }

  abs(amount: number): number {
    return Math.abs(amount ?? 0);
  }

  getIncomePercentage(amount: number): number {
    const income = this.totals().income;

    if (income <= 0) {
      return 0;
    }

    return (Math.abs(amount) / income) * 100;
  }

  getIncomeBarPercentage(amount: number): number {
    return Math.min(100, Math.max(0, this.getIncomePercentage(amount)));
  }

  getRemainingIncome(): number {
    const totals = this.totals();

    return (
      totals.income -
      Math.abs(totals.expense) -
      Math.abs(totals.investment) -
      Math.abs(totals.transfer)
    );
  }

  getRemainingPercentage(): number {
    const income = this.totals().income;

    if (income <= 0) {
      return 0;
    }

    return (this.getRemainingIncome() / income) * 100;
  }

  getRemainingBarPercentage(): number {
    return Math.min(100, Math.max(0, this.getRemainingPercentage()));
  }

  getTotalAllocatedPercentage(): number {
    const totals = this.totals();

    return this.getIncomePercentage(
      Math.abs(totals.expense) + Math.abs(totals.investment) + Math.abs(totals.transfer),
    );
  }

  private updateCharts(): void {
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
