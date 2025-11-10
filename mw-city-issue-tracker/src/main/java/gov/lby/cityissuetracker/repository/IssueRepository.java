package gov.lby.cityissuetracker.repository;

import gov.lby.cityissuetracker.entity.Issue;
import gov.lby.cityissuetracker.entity.IssueStatus;
import gov.lby.cityissuetracker.entity.IssueCategory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IssueRepository extends JpaRepository<Issue, UUID> {
    // Spring Data JPA automatically implements these methods:
    
    // Find issues by status with pagination
    Page<Issue> findByStatus(IssueStatus status, Pageable pageable);
    
    // Find issues by category with pagination
    Page<Issue> findByCategory(IssueCategory category, Pageable pageable);
    
    // Custom query using @Query annotation
    @Query("SELECT i FROM Issue i WHERE i.title LIKE %:keyword% OR i.description LIKE %:keyword%")
    Page<Issue> searchByKeyword(String keyword, Pageable pageable);
}