import { Component, DestroyRef, inject, signal } from '@angular/core';

import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';

import { filter } from 'rxjs/operators';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { CommonService } from '../service/common-service';

interface MenuItem {
  label: string;
  icon: string;
  route?: string;
  children?: MenuItem[];
}

@Component({
  selector: 'app-menu',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './menu.html',
  styleUrl: './menu.scss',
})
export class Menu {
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly _cs = inject(CommonService);

  readonly collapsed = signal(false);

  // Mobile detection
  readonly isMobile = signal(this._cs.isMobile());

  // Expanded menu items
  readonly expandedItems = signal<Set<string>>(
    new Set(['Financial Transaction', 'Category Management', 'Settings']),
  );

  readonly currentUrl = signal(this.router.url);

  readonly menuItems: MenuItem[] = [
    {
      label: 'Dashboard',
      icon: 'dashboard',
      route: '/dashboard',
    },
    {
      label: 'Financial Transaction',
      icon: 'receipt_long',
      children: [
        {
          label: 'All Transactions',
          icon: 'list_alt',
          route: '/allTransactions',
        },
        {
          label: 'Expense',
          icon: 'account_balance_wallet',
          route: '/expenses',
        },
        {
          label: 'Income',
          icon: 'payments',
          route: '/income',
        },
        {
          label: 'Investment',
          icon: 'account_balance',
          route: '/investments',
        },
        {
          label: 'Transfer',
          icon: 'swap_horiz',
          route: '/transfers',
        },
      ],
    },
    {
      label: 'Category Management',
      icon: 'category',
      children: [
        {
          label: 'Category',
          icon: 'category',
          route: '/categories',
        },
        {
          label: 'Subcategory',
          icon: 'account_tree',
          route: '/subcategories',
        },
      ],
    },
    {
      label: 'Settings',
      icon: 'settings',
      children: [
        {
          label: 'System Config',
          icon: 'settings_applications',
          route: '/system-config',
        },
        {
          label: 'Reference Objects',
          icon: 'data_object',
          route: '/reference-objects',
        },
        {
          label: 'Reference Values',
          icon: 'fact_check',
          route: '/reference-values',
        },
        {
          label: 'Recurring Transactions',
          icon: 'event_repeat',
          route: '/recurringTransactions',
        },
      ],
    },
    {
      label: 'Bulk Upload',
      icon: 'upload_file',
      route: '/bulkupload',
    },
    {
      label: 'User Management',
      icon: 'group',
      route: '/users',
    },
    {
      label: 'Downloads',
      icon: 'download',
      route: '/downloads',
    },
  ];

  constructor() {
    // Expand all submenu items on mobile
    if (this.isMobile()) {
      this.expandAllMenus();
    }

    this.router.events
      .pipe(
        filter((event): event is NavigationEnd => event instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((event) => {
        this.currentUrl.set(event.urlAfterRedirects);

        this.expandActiveParent();
      });

    this.expandActiveParent();
  }

  // Expand all parent menus
  private expandAllMenus(): void {
    this.expandedItems.set(
      new Set(this.menuItems.filter((item) => item.children?.length).map((item) => item.label)),
    );
  }

  // Sidebar collapse / expand
  toggleSidebar(): void {
    if (this.isMobile()) {
      return;
    }

    this.collapsed.update((value) => !value);
  }

  // Parent menu toggle
  toggleMenu(item: MenuItem): void {
    if (!item.children?.length) {
      return;
    }

    // On mobile, all menus remain expanded
    if (this.isMobile()) {
      this.expandAllMenus();
      return;
    }

    if (this.collapsed()) {
      this.collapsed.set(false);
    }

    this.expandedItems.update((expanded) => {
      const next = new Set(expanded);

      if (next.has(item.label)) {
        next.delete(item.label);
      } else {
        next.add(item.label);
      }

      return next;
    });
  }

  isExpanded(item: MenuItem): boolean {
    return this.isMobile() || this.expandedItems().has(item.label);
  }

  // Check active menu
  isActive(item: MenuItem): boolean {
    const url = this.currentUrl();

    if (item.route) {
      return this.isRouteActive(url, item.route);
    }

    if (item.children?.length) {
      return item.children.some((child) => child.route && this.isRouteActive(url, child.route));
    }

    return false;
  }

  // Route matching
  private isRouteActive(currentUrl: string, route: string): boolean {
    const currentPath = currentUrl.split('?')[0].split('#')[0];

    return currentPath === route || currentPath.startsWith(`${route}/`);
  }

  // Automatically expand active parent
  private expandActiveParent(): void {
    const url = this.currentUrl();

    this.expandedItems.update((expanded) => {
      const next = new Set(expanded);

      for (const item of this.menuItems) {
        if (!item.children?.length) {
          continue;
        }

        const childIsActive = item.children.some(
          (child) => child.route && this.isRouteActive(url, child.route),
        );

        if (childIsActive) {
          next.add(item.label);
        }
      }

      return next;
    });
  }
}
