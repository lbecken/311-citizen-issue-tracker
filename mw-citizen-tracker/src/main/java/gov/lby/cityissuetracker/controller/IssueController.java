package gov.lby.cityissuetracker.controller;

import gov.lby.cityissuetracker.dto.CreateIssueRequest;
import gov.lby.cityissuetracker.dto.IssueResponse;
import gov.lby.cityissuetracker.dto.UpdateStatusRequest;
import gov.lby.cityissuetracker.entity.IssueStatus;
import gov.lby.cityissuetracker.service.IssueService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Swagger
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

@Tag(name = "Issues", description = "Issue reporting and management APIs")
@RestController // = @Controller + @ResponseBody (auto-serializes to JSON)
@RequestMapping("/api/v1/issues") // Base URL for all methods
@RequiredArgsConstructor
public class IssueController {
    
    private final IssueService issueService;
    
    // POST /api/v1/issues
    @PostMapping
    public ResponseEntity<IssueResponse> createIssue(@Valid @RequestBody CreateIssueRequest request) {
        // @Valid triggers Bean Validation on the DTO
        // @RequestBody converts incoming JSON to CreateIssueRequest object
        IssueResponse response = issueService.createIssue(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
        // Returns 201 Created status with the new issue in body
    }
    
    // GET /api/v1/issues/{id}
    @GetMapping("/{id}")
    public ResponseEntity<IssueResponse> getIssue(@PathVariable UUID id) {
        // @PathVariable extracts 'id' from the URL path
        return ResponseEntity.ok(issueService.getIssue(id));
        // .ok() = 200 OK status
    }
    
    // GET /api/v1/issues?page=0&size=20&status=REPORTED
    @GetMapping
    public ResponseEntity<Page<IssueResponse>> getAllIssues(
        @RequestParam(defaultValue = "0") int page, // Query param with default
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String status, // Optional param
        @RequestParam(required = false) String category
    ) {
        // @RequestParam extracts values from URL query string
        return ResponseEntity.ok(issueService.getAllIssues(page, size, status, category));
    }
    
    // PATCH /api/v1/issues/{id}/status
    @PatchMapping("/{id}/status")
    public ResponseEntity<IssueResponse> updateStatus(
        @PathVariable UUID id,
        @RequestBody @Valid UpdateStatusRequest request // Custom DTO for status update
    ) {
        return ResponseEntity.ok(issueService.updateStatus(id, IssueStatus.valueOf(request.getStatus()), request.getNotes()));
    }
}