package gov.lby.cityissuetracker.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.PrePersist;
//import jakarta.persistence.UUID; //??
import java.util.UUID;
// Jakarta Bean Validation
import jakarta.validation.constraints.NotBlank; // null/empty/whitespace
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Email;
// Lombok
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.locationtech.jts.geom.Point; // PostGIS geometry type (lat/long)

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "issues")
@Data // Lombok: generates getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok: generates empty constructor (required by JPA)
@AllArgsConstructor // Lombok: generates constructor with all fields
@Builder // Lombok: enables Issue.builder().title("...").build() pattern
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueStatus status = IssueStatus.REPORTED;

    @Min(1) @Max(5)
    @Column(nullable = false)
    private Integer priority = 3;

    @Column(columnDefinition = "Geometry(Point, 4326)", nullable = false)
    private Point location;

    private String address;

    @NotBlank
    @Column(nullable = false)
    private String reportedBy;

    @Email
    @Column
    private String reporterEmail;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // TODO: Uncomment when Department entity is created
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "department_id") // Foreign key column in 'issues' table
    // private Department department;

    @Column(name = "worker_id")
    private String workerId;

    // Lifecycle callback: auto-update 'updatedAt' before save
    @PreUpdate
    @PrePersist
    public void updateTimestamps() {
        updatedAt = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
