import { Injectable, NgZone } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject, BehaviorSubject } from 'rxjs';

// Interface for dashboard metrics (matches backend DashboardMetrics DTO)
export interface DashboardMetrics {
  totalIssues: number;
  openIssues: number;
  resolvedIssues: number;
  closedIssues: number;
  issuesByStatus: { [key: string]: number };
  issuesByCategory: { [key: string]: number };
  issuesByPriority: { [key: string]: number };
  issuesCreatedLast5Minutes?: number;
  issuesResolvedLast5Minutes?: number;
  timestamp: string;
}

// Interface for issue events from SSE stream
export interface IssueEvent {
  issueId: string;
  eventType: 'CREATED' | 'STATUS_CHANGED' | 'RESOLVED' | 'CLOSED' | 'ASSIGNED' | 'PRIORITY_CHANGED';
  status: string;
  previousStatus?: string;
  category: string;
  priority: number;
  timestamp: string;
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private apiUrl = 'http://localhost:8080/api/v1/dashboard';
  private eventSource: EventSource | null = null;

  // Subjects for real-time updates
  private metricsSubject = new BehaviorSubject<DashboardMetrics | null>(null);
  private eventSubject = new Subject<IssueEvent>();
  private connectionStatusSubject = new BehaviorSubject<'connected' | 'disconnected' | 'connecting'>('disconnected');

  // Public observables
  metrics$ = this.metricsSubject.asObservable();
  events$ = this.eventSubject.asObservable();
  connectionStatus$ = this.connectionStatusSubject.asObservable();

  constructor(
    private http: HttpClient,
    private ngZone: NgZone
  ) {}

  /**
   * Get current dashboard metrics (one-time fetch)
   */
  getMetrics(): Observable<DashboardMetrics> {
    return this.http.get<DashboardMetrics>(`${this.apiUrl}/metrics`);
  }

  /**
   * Subscribe to real-time dashboard updates via SSE
   */
  subscribeToUpdates(): void {
    if (this.eventSource) {
      return; // Already connected
    }

    this.connectionStatusSubject.next('connecting');
    this.eventSource = new EventSource(`${this.apiUrl}/stream`);

    // Handle metrics updates
    this.eventSource.addEventListener('metrics', (event: MessageEvent) => {
      this.ngZone.run(() => {
        try {
          const metrics: DashboardMetrics = JSON.parse(event.data);
          this.metricsSubject.next(metrics);
        } catch (e) {
          console.error('Failed to parse metrics event:', e);
        }
      });
    });

    // Handle issue events
    this.eventSource.addEventListener('issue-event', (event: MessageEvent) => {
      this.ngZone.run(() => {
        try {
          const issueEvent: IssueEvent = JSON.parse(event.data);
          this.eventSubject.next(issueEvent);
        } catch (e) {
          console.error('Failed to parse issue event:', e);
        }
      });
    });

    // Handle connection open
    this.eventSource.onopen = () => {
      this.ngZone.run(() => {
        this.connectionStatusSubject.next('connected');
        console.log('SSE connection established');
      });
    };

    // Handle errors
    this.eventSource.onerror = (error) => {
      this.ngZone.run(() => {
        console.error('SSE connection error:', error);
        this.connectionStatusSubject.next('disconnected');

        // Attempt to reconnect after 5 seconds
        setTimeout(() => {
          if (this.eventSource?.readyState === EventSource.CLOSED) {
            this.disconnect();
            this.subscribeToUpdates();
          }
        }, 5000);
      });
    };
  }

  /**
   * Disconnect from SSE stream
   */
  disconnect(): void {
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
      this.connectionStatusSubject.next('disconnected');
      console.log('SSE connection closed');
    }
  }

  /**
   * Get current metrics value (synchronous)
   */
  getCurrentMetrics(): DashboardMetrics | null {
    return this.metricsSubject.getValue();
  }
}
