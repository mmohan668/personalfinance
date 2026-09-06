import { Component, DestroyRef, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { filter } from 'rxjs/operators';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

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

  /**
   * Whether the entire sidebar is collapsed.
   *
   * false = 260px sidebar
   * true  = 72px sidebar
   */
  readonly collapsed = signal(false);

  readonly menuItems: MenuItem[] = [
    {
      label: 'Dashboard',
      icon: 'dashboard',
      route: '/dashboard',
    },
    {
      label: 'Expense Management',
      icon: 'receipt_long',
      children: [
        {
          label: 'Expense',
          icon: 'payments',
          route: '/expenses',
        },
        {
          label: 'Income',
          icon: 'trending_up',
          route: '/income',
        },
        {
          label: 'Transfer',
          icon: 'swap_horiz',
          route: '/transfers',
        },
        {
          label: 'Investment',
          icon: 'account_balance',
          route: '/investments',
        },
      ],
    },
    {
      label: 'Category Management',
      icon: 'category',
      children: [
        {
          label: 'Category',
          icon: 'label',
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
          icon: 'tune',
          route: '/system-config',
        },
        {
          label: 'Reference Values',
          icon: 'list_alt',
          route: '/reference-values',
        },
      ],
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

  /**
   * Parent menus expanded by default.
   */
  readonly expandedItems = signal<Set<string>>(
    new Set(['Expense Management', 'Category Management', 'Settings']),
  );

  readonly currentUrl = signal(this.router.url);

  constructor() {
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

  /**
   * Collapse / expand the entire sidebar.
   */
  toggleSidebar(): void {
    this.collapsed.update((value) => !value);
  }

  /**
   * Expand / collapse a submenu.
   */
  toggleMenu(item: MenuItem): void {
    if (!item.children?.length) {
      return;
    }

    // If sidebar is collapsed, clicking a parent
    // first expands the sidebar.
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
    return this.expandedItems().has(item.label);
  }

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

  private isRouteActive(currentUrl: string, route: string): boolean {
    const currentPath = currentUrl.split('?')[0].split('#')[0];

    return currentPath === route || currentPath.startsWith(`${route}/`);
  }

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
