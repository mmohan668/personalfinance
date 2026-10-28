import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { UserMenu } from '../modules/shared/user-menu/user-menu';
import { Menu } from '../modules/shared/menu/menu';

@Component({
  imports: [UserMenu, Menu, RouterOutlet],
  standalone: true,
  selector: 'app-layout',
  styleUrl: './layout.scss',
  templateUrl: './layout.html',
})
export class Layout {}
