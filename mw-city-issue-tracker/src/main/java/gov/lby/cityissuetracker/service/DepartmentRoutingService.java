package gov.lby.cityissuetracker.service;

import gov.lby.cityissuetracker.entity.Department;
import gov.lby.cityissuetracker.repository.DepartmentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentRoutingService {

    private final ChatClient.Builder chatClientBuilder;
    private final DepartmentRepository departmentRepository;

    /**
     * Routes an issue to the most appropriate department using AI analysis.
     *
     * @param title Issue title
     * @param description Issue description
     * @param category Issue category (e.g., POTHOLE, STREETLIGHT)
     * @return The selected Department entity
     */
    public Department routeIssue(String title, String description, String category) {
        String departments = departmentRepository.findAll().stream()
                .map(d -> d.getName() + ": " + d.getEmoji())
                .collect(Collectors.joining(", "));

        String prompt = """
                You are a city 311 system router. Given an issue, select the BEST department.

                Departments available: %s

                Issue: %s
                Description: %s
                Category: %s

                Respond ONLY with the exact department name. If uncertain, respond "Sanitation".
                """.formatted(departments, title, description != null ? description : "No description provided", category);

        try {
            ChatClient chatClient = chatClientBuilder.build();
            String departmentName = chatClient.prompt(prompt).call().content();

            // Clean up the response (remove quotes, extra whitespace)
            departmentName = departmentName.trim().replaceAll("^\"|\"$", "");

            log.info("AI routed issue '{}' to department: {}", title, departmentName);

            return departmentRepository.findByNameIgnoreCase(departmentName)
                    .orElseGet(() -> {
                        log.warn("Department '{}' not found, defaulting to Sanitation", departmentName);
                        return departmentRepository.findByName("Sanitation")
                                .orElseThrow(() -> new RuntimeException("Default department 'Sanitation' not found"));
                    });
        } catch (Exception e) {
            log.error("AI routing failed, defaulting to Sanitation: {}", e.getMessage());
            return departmentRepository.findByName("Sanitation")
                    .orElseThrow(() -> new RuntimeException("Default department 'Sanitation' not found"));
        }
    }
}
