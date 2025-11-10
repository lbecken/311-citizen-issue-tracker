import { Routes } from '@angular/router';
import { IssueFormComponent } from './components/issue-form/issue-form.component';
import { IssueListComponent } from './components/issue-list/issue-list.component';

export const routes: Routes = [
  // Home page: show issue form
  { path: '', redirectTo: '/report', pathMatch: 'full' },
  
  // Report new issue page
  { path: 'report', component: IssueFormComponent },
  
  // List all issues page
  { path: 'issues', component: IssueListComponent },
  
  // View single issue (placeholder for future detail view)
  { path: 'issues/:id', component: IssueListComponent }, // Reuse list for now
  
  // Wildcard route: redirect to home if URL not found
  { path: '**', redirectTo: '/report' }
];