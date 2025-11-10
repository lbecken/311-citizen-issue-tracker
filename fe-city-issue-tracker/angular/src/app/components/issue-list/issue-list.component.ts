import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';
import { IssueService, Issue, Page } from '../../services/issue.service';
import { RouterModule } from '@angular/router';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'app-issue-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule, // added
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatIconModule,
    MatTooltipModule, // added
    FormsModule
  ],
  templateUrl: './issue-list.component.html',
  styleUrls: ['./issue-list.component.scss']
})
export class IssueListComponent implements OnInit {
  // Data source for the table
  issues: Issue[] = [];
  
  // Pagination info from backend
  totalElements = 0;
  totalPages = 0;
  pageSize = 20;
  currentPage = 0;
  
  // Filters
  statusFilter = '';
  categoryFilter = '';
  
  // Define table columns
  displayedColumns: string[] = [
    'id',
    'title',
    'category',
    'status',
    'priority',
    'reportedBy',
    'createdAt',
    'actions'
  ];

  // Categories and statuses for filter dropdowns
  categories = ['POTHOLE', 'STREETLIGHT', 'GRAFFITI', 'TRASH', 'NOISE', 'OTHER'];
  statuses = ['REPORTED', 'VALIDATED', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'];

  constructor(private issueService: IssueService) {}

  ngOnInit(): void {
    this.loadIssues();
  }

  // Load issues from backend
  loadIssues(): void {
    this.issueService.getAllIssues(
      this.currentPage,
      this.pageSize,
      this.statusFilter,
      this.categoryFilter
    ).subscribe({
      next: (page: Page<Issue>) => {
        this.issues = page.content;
        this.totalElements = page.totalElements;
        this.totalPages = page.totalPages;
      },
      error: (error) => {
        console.error('Failed to load issues:', error);
      }
    });
  }

  // Handle page change event from paginator
  onPageChange(event: PageEvent): void {
    this.pageSize = event.pageSize;
    this.currentPage = event.pageIndex;
    this.loadIssues();
  }

  // Apply filters
  applyFilters(): void {
    this.currentPage = 0; // Reset to first page
    this.loadIssues();
  }

  // Clear filters
  clearFilters(): void {
    this.statusFilter = '';
    this.categoryFilter = '';
    this.loadIssues();
  }

  // Get status chip color (for UI styling)
  getStatusColor(status: string): string {
    const colors: { [key: string]: string } = {
      'REPORTED': 'primary',
      'VALIDATED': 'accent',
      'ASSIGNED': 'warn',
      'IN_PROGRESS': 'warn',
      'RESOLVED': 'primary',
      'CLOSED': ''
    };
    return colors[status] || '';
  }

  // Get category emoji (for better UI)
  getCategoryEmoji(category: string): string {
    const emojis: { [key: string]: string } = {
      'POTHOLE': '🕳️',
      'STREETLIGHT': '💡',
      'GRAFFITI': '🎨',
      'TRASH': '🗑️',
      'NOISE': '🔊',
      'OTHER': '📦'
    };
    return emojis[category] || '📦';
  }

  // Format date for display
  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString();
  }
}
