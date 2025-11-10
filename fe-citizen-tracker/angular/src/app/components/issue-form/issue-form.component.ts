import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { IssueService, CreateIssueRequest } from '../../services/issue.service';
import { Issue } from '../../services/issue.service';

@Component({
  selector: 'app-issue-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatCardModule
  ],
  templateUrl: './issue-form.component.html',
  styleUrls: ['./issue-form.component.scss']
})
export class IssueFormComponent {
  // Define form group with validation rules
  issueForm: FormGroup;
  
  // Categories for the dropdown
  categories = [
    { value: 'POTHOLE', label: '🕳️ Pothole' },
    { value: 'STREETLIGHT', label: '💡 Street Light' },
    { value: 'GRAFFITI', label: '🎨 Graffiti' },
    { value: 'TRASH', label: '🗑️ Trash/Illegal Dumping' },
    { value: 'NOISE', label: '🔊 Noise Complaint' },
    { value: 'OTHER', label: '📦 Other' }
  ];

  constructor(
    private fb: FormBuilder, // FormBuilder makes creating forms easier
    private issueService: IssueService
  ) {
    // Initialize the form with fields and validators
    this.issueForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(200)]],
      description: ['', Validators.maxLength(2000)],
      category: ['', Validators.required],
      latitude: ['', [Validators.required, Validators.min(-90), Validators.max(90)]],
      longitude: ['', [Validators.required, Validators.min(-180), Validators.max(180)]],
      address: [''],
      reportedBy: ['', Validators.required],
      reporterEmail: ['', [Validators.required, Validators.email]]
    });
  }

  // Convenience getter for form fields (makes template cleaner)
  get f() {
    return this.issueForm.controls;
  }

  // Handle form submission
  onSubmit(): void {
    // Mark all fields as touched to show validation errors
    if (this.issueForm.invalid) {
      this.issueForm.markAllAsTouched();
      return;
    }

    // Convert form value to CreateIssueRequest
    const request: CreateIssueRequest = this.issueForm.value;
    
    // Call the service
    this.issueService.createIssue(request).subscribe({
      next: (issue: Issue) => {
        console.log('Issue created:', issue);
        alert(`Issue reported successfully! ID: ${issue.id}`);
        this.issueForm.reset(); // Clear the form
      },
      error: (error) => {
        console.error('Error creating issue:', error);
        alert('Failed to create issue. Please try again.');
      }
    });
  }

  // Reset form button
  onReset(): void {
    this.issueForm.reset();
  }
}
