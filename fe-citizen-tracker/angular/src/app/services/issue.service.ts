import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
//import { Page } from '../models/page.model';

// Interface for creating an issue (matches backend DTO)
export interface CreateIssueRequest {
  title: string;
  description?: string;
  category: 'POTHOLE' | 'STREETLIGHT' | 'GRAFFITI' | 'TRASH' | 'NOISE' | 'OTHER';
  latitude: number;
  longitude: number;
  address?: string;
  reportedBy: string;
  reporterEmail: string;
}

// Interface for the issue response (matches backend DTO)
export interface Issue {
  id: string; // UUID as string
  title: string;
  description: string;
  category: string;
  status: 'REPORTED' | 'VALIDATED' | 'ASSIGNED' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';
  priority: number;
  latitude: number;
  longitude: number;
  address?: string;
  reportedBy: string;
  reporterEmail: string;
  createdAt: string; // ISO date string
  updatedAt: string;
  departmentName?: string;
  workerId?: string;
}

// Generic page response interface
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number; // current page number
  first: boolean;
  last: boolean;
}

@Injectable({
  providedIn: 'root' // Makes this service available app-wide without importing in providers
})
export class IssueService {
  // Base URL for all API calls
  private apiUrl = 'http://localhost:8080/api/v1/issues';
  
  constructor(private http: HttpClient) {}

  /**
   * Create a new issue
   * @param issue - The issue data to send
   * @returns Observable that emits the created issue
   */
  createIssue(issue: CreateIssueRequest): Observable<Issue> {
    // POST request to /api/v1/issues
    return this.http.post<Issue>(this.apiUrl, issue);
  }

  /**
   * Get a single issue by ID
   * @param id - The UUID of the issue
   * @returns Observable that emits the issue
   */
  getIssue(id: string): Observable<Issue> {
    return this.http.get<Issue>(`${this.apiUrl}/${id}`);
  }

  /**
   * Get paginated list of issues with optional filters
   * @param page - Page number (0-based)
   * @param size - Number of items per page
   * @param status - Optional status filter
   * @param category - Optional category filter
   * @returns Observable that emits a page of issues
   */
  getAllIssues(page: number = 0, size: number = 20, status?: string, category?: string): Observable<Page<Issue>> {
    // Build query parameters
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    if (status) {
      params = params.set('status', status);
    }
    if (category) {
      params = params.set('category', category);
    }

    // GET request with query parameters
    return this.http.get<Page<Issue>>(this.apiUrl, { params });
  }

  /**
   * Update issue status
   * @param id - The issue UUID
   * @param status - New status
   * @param notes - Optional notes
   * @returns Observable that emits the updated issue
   */
  updateStatus(id: string, status: string, notes?: string): Observable<Issue> {
    const body = { status, notes };
    return this.http.patch<Issue>(`${this.apiUrl}/${id}/status`, body);
  }
}