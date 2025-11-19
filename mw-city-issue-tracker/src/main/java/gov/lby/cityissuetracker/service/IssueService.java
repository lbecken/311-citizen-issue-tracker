package gov.lby.cityissuetracker.service;

import gov.lby.cityissuetracker.dto.CreateIssueRequest;
import gov.lby.cityissuetracker.dto.IssueResponse;
import gov.lby.cityissuetracker.entity.Issue;
import gov.lby.cityissuetracker.entity.IssueStatus;
import gov.lby.cityissuetracker.entity.IssueCategory;
import gov.lby.cityissuetracker.repository.IssueRepository;
import gov.lby.cityissuetracker.exception.IssueNotFoundException;

import gov.lby.cityissuetracker.event.IssueCreatedApplicationEvent;

import lombok.RequiredArgsConstructor;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor // Lombok: auto-creates constructor for final fields
@Transactional // All methods run in a transaction (auto-rollback on exceptions)
public class IssueService {

    private final IssueRepository issueRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final GeometryFactory geometryFactory = new GeometryFactory(); // For creating Point objects
    
    public IssueResponse createIssue(CreateIssueRequest request) {
        // Convert DTO to Entity
        Point location = geometryFactory.createPoint(new Coordinate(request.getLongitude(), request.getLatitude()));
        
        Issue issue = Issue.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .category(IssueCategory.valueOf(request.getCategory()))
            .status(IssueStatus.REPORTED)
            .priority(3) // Default priority
            .location(location)
            .address(request.getAddress())
            .reportedBy(request.getReportedBy())
            .reporterEmail(request.getReporterEmail())
            .build();
        
        Issue saved = issueRepository.save(issue);

        // Publish Spring application event - RabbitMQ message will be sent after transaction commits
        eventPublisher.publishEvent(new IssueCreatedApplicationEvent(this, saved.getId()));

        // Convert Entity to DTO
        return toResponse(saved);
    }
    
    public IssueResponse getIssue(UUID id) {
        Issue issue = issueRepository.findById(id)
            .orElseThrow(() -> new IssueNotFoundException("Issue not found: " + id));
        return toResponse(issue);
    }
    
    public Page<IssueResponse> getAllIssues(int page, int size, String status, String category) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        if (status != null) {
            return issueRepository.findByStatus(IssueStatus.valueOf(status), pageRequest)
                .map(this::toResponse);
        }
        if (category != null) {
            return issueRepository.findByCategory(IssueCategory.valueOf(category), pageRequest)
                .map(this::toResponse);
        }
        
        return issueRepository.findAll(pageRequest).map(this::toResponse);
    }
    
    public IssueResponse updateStatus(UUID id, IssueStatus newStatus, String notes) {
        Issue issue = issueRepository.findById(id)
            .orElseThrow(() -> new IssueNotFoundException("Issue not found: " + id));
        
        issue.setStatus(newStatus);
        // In real app, you might save notes to a separate audit table
        return toResponse(issueRepository.save(issue));
    }
    
    // Helper method to convert Entity → DTO
    private IssueResponse toResponse(Issue issue) {
        return IssueResponse.builder()
            .id(issue.getId())
            .title(issue.getTitle())
            .description(issue.getDescription())
            .category(issue.getCategory())
            .status(issue.getStatus())
            .priority(issue.getPriority())
            .latitude(issue.getLocation().getY()) // PostGIS point stores lat/lng
            .longitude(issue.getLocation().getX())
            .address(issue.getAddress())
            .reportedBy(issue.getReportedBy())
            .reporterEmail(issue.getReporterEmail())
            .createdAt(issue.getCreatedAt())
            .updatedAt(issue.getUpdatedAt())
            // TODO: Uncomment when Department entity is created
            // .departmentName(issue.getDepartment() != null ? issue.getDepartment().getName() : null)
            .departmentName(null)
            .workerId(issue.getWorkerId())
            .build();
    }
}