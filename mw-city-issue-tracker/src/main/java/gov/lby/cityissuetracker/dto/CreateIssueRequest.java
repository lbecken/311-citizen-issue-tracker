package gov.lby.cityissuetracker.dto;

// Validation
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Email;

// Lombok
import lombok.Data;

@Data
public class CreateIssueRequest {
    
    @NotBlank(message = "Title is required") // Custom error message
    @Size(max = 200, message = "Title must be under 200 characters")
    private String title;

    @Size(max = 2000, message = "Description is too long")
    private String description;

    @NotNull(message = "Category is required")
    private String category; // String because enum might not deserialize cleanly

    @NotNull(message = "Location is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;

    @NotNull(message = "Location is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;

    private String address; // Optional, can be auto-filled

    @NotBlank(message = "Your name is required")
    private String reportedBy;

    @Email(message = "Invalid email format")
    private String reporterEmail;
}