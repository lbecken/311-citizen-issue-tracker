import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatBadgeModule } from '@angular/material/badge';
import { Subscription } from 'rxjs';
import { DashboardService, DashboardMetrics, IssueEvent } from '../../services/dashboard.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatDividerModule,
    MatBadgeModule,
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DashboardComponent implements OnInit, OnDestroy {
  metrics: DashboardMetrics | null = null;
  connectionStatus: 'connected' | 'disconnected' | 'connecting' = 'disconnected';
  recentEvents: IssueEvent[] = [];

  private subscriptions: Subscription[] = [];

  // Status labels for display
  statusLabels: { [key: string]: string } = {
    REPORTED: 'Reported',
    VALIDATED: 'Validated',
    ASSIGNED: 'Assigned',
    IN_PROGRESS: 'In Progress',
    RESOLVED: 'Resolved',
    CLOSED: 'Closed',
  };

  // Category labels for display
  categoryLabels: { [key: string]: string } = {
    POTHOLE: 'Pothole',
    STREETLIGHT: 'Streetlight',
    GRAFFITI: 'Graffiti',
    TRASH: 'Trash',
    NOISE: 'Noise',
    OTHER: 'Other',
  };

  // Priority labels
  priorityLabels: { [key: string]: string } = {
    '1': 'Critical',
    '2': 'High',
    '3': 'Medium',
    '4': 'Low',
    '5': 'Very Low',
  };

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    // Subscribe to metrics updates
    this.subscriptions.push(
      this.dashboardService.metrics$.subscribe((metrics) => {
        this.metrics = metrics;
      })
    );

    // Subscribe to connection status
    this.subscriptions.push(
      this.dashboardService.connectionStatus$.subscribe((status) => {
        this.connectionStatus = status;
      })
    );

    // Subscribe to issue events
    this.subscriptions.push(
      this.dashboardService.events$.subscribe((event) => {
        // Keep last 10 events
        this.recentEvents = [event, ...this.recentEvents].slice(0, 10);
      })
    );

    // Start SSE connection
    this.dashboardService.subscribeToUpdates();
  }

  ngOnDestroy(): void {
    // Cleanup subscriptions
    this.subscriptions.forEach((sub) => sub.unsubscribe());

    // Disconnect from SSE
    this.dashboardService.disconnect();
  }

  // Get status color for styling
  getStatusColor(status: string): string {
    const colors: { [key: string]: string } = {
      REPORTED: '#2196f3',
      VALIDATED: '#9c27b0',
      ASSIGNED: '#ff9800',
      IN_PROGRESS: '#ff5722',
      RESOLVED: '#4caf50',
      CLOSED: '#757575',
    };
    return colors[status] || '#757575';
  }

  // Get category color for styling
  getCategoryColor(category: string): string {
    const colors: { [key: string]: string } = {
      POTHOLE: '#795548',
      STREETLIGHT: '#ffc107',
      GRAFFITI: '#e91e63',
      TRASH: '#8bc34a',
      NOISE: '#00bcd4',
      OTHER: '#607d8b',
    };
    return colors[category] || '#607d8b';
  }

  // Get priority color for styling
  getPriorityColor(priority: string): string {
    const colors: { [key: string]: string } = {
      '1': '#f44336',
      '2': '#ff9800',
      '3': '#ffc107',
      '4': '#8bc34a',
      '5': '#4caf50',
    };
    return colors[priority] || '#757575';
  }

  // Format event type for display
  formatEventType(eventType: string): string {
    const labels: { [key: string]: string } = {
      CREATED: 'New Issue',
      STATUS_CHANGED: 'Status Changed',
      RESOLVED: 'Resolved',
      CLOSED: 'Closed',
      ASSIGNED: 'Assigned',
      PRIORITY_CHANGED: 'Priority Changed',
    };
    return labels[eventType] || eventType;
  }

  // Format timestamp for display
  formatTime(timestamp: string): string {
    const date = new Date(timestamp);
    return date.toLocaleTimeString();
  }

  // Get sorted status entries
  getSortedStatuses(): [string, number][] {
    if (!this.metrics?.issuesByStatus) return [];
    const statusOrder = ['REPORTED', 'VALIDATED', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'];
    return statusOrder
      .filter((status) => this.metrics!.issuesByStatus[status] !== undefined)
      .map((status) => [status, this.metrics!.issuesByStatus[status]]);
  }

  // Get sorted category entries
  getSortedCategories(): [string, number][] {
    if (!this.metrics?.issuesByCategory) return [];
    return Object.entries(this.metrics.issuesByCategory).sort((a, b) => b[1] - a[1]);
  }

  // Get sorted priority entries
  getSortedPriorities(): [string, number][] {
    if (!this.metrics?.issuesByPriority) return [];
    return Object.entries(this.metrics.issuesByPriority).sort((a, b) => parseInt(a[0]) - parseInt(b[0]));
  }

  // Calculate percentage for progress bars
  getPercentage(value: number, total: number): number {
    if (total === 0) return 0;
    return Math.round((value / total) * 100);
  }
}
