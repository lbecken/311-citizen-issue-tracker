/*
import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected readonly title = signal('citizen-tracker');
}
*/

import { Component, signal } from '@angular/core';
//import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule
  ],
  template: `
    <mat-toolbar color="primary">
      <span>CityTracker 311</span>

      <nav class="nav-links">
        <a mat-button routerLink="/report" routerLinkActive="active">
          <mat-icon>add_circle</mat-icon> Report Issue
        </a>
        <a mat-button routerLink="/issues" routerLinkActive="active">
          <mat-icon>list</mat-icon> View Issues
        </a>
        <a mat-button routerLink="/dashboard" routerLinkActive="active">
          <mat-icon>dashboard</mat-icon> Dashboard
        </a>
      </nav>
    </mat-toolbar>

    <main class="main-content">
      <router-outlet></router-outlet> <!-- This is where components load -->
    </main>
  `,
  styles: [`
    :host {
      display: block;
      min-height: 100vh;
      background: #f5f5f5;
    }
    
    mat-toolbar {
      position: sticky;
      top: 0;
      z-index: 1000;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }
    
    .nav-links {
      margin-left: auto;
      display: flex;
      gap: 10px;
    }
    
    .nav-links a {
      display: flex;
      align-items: center;
      gap: 5px;
    }
    
    .nav-links a.active {
      background: rgba(255,255,255,0.2);
    }
    
    .main-content {
      padding: 20px;
    }
  `]
})
export class App {
  protected readonly title = signal('citizen-tracker');
}
//export class AppComponent {
//  title = 'CityTracker 311';
//}
