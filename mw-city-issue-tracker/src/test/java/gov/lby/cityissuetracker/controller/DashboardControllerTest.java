package gov.lby.cityissuetracker.controller;

import gov.lby.cityissuetracker.kafka.event.DashboardMetrics;
import gov.lby.cityissuetracker.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @Test
    void getMetrics_shouldReturnDashboardMetrics() throws Exception {
        // Given
        DashboardMetrics metrics = DashboardMetrics.builder()
                .totalIssues(100)
                .openIssues(60)
                .resolvedIssues(30)
                .closedIssues(10)
                .issuesByStatus(Map.of(
                        "REPORTED", 20L,
                        "VALIDATED", 15L,
                        "ASSIGNED", 10L,
                        "IN_PROGRESS", 15L,
                        "RESOLVED", 30L,
                        "CLOSED", 10L
                ))
                .issuesByCategory(Map.of(
                        "POTHOLE", 40L,
                        "STREETLIGHT", 25L,
                        "GRAFFITI", 15L,
                        "TRASH", 10L,
                        "NOISE", 5L,
                        "OTHER", 5L
                ))
                .issuesByPriority(Map.of(
                        "1", 10L,
                        "2", 20L,
                        "3", 40L,
                        "4", 20L,
                        "5", 10L
                ))
                .timestamp(Instant.now())
                .build();

        when(dashboardService.getMetrics()).thenReturn(metrics);

        // When/Then
        mockMvc.perform(get("/api/v1/dashboard/metrics"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalIssues").value(100))
                .andExpect(jsonPath("$.openIssues").value(60))
                .andExpect(jsonPath("$.resolvedIssues").value(30))
                .andExpect(jsonPath("$.closedIssues").value(10))
                .andExpect(jsonPath("$.issuesByStatus.REPORTED").value(20))
                .andExpect(jsonPath("$.issuesByCategory.POTHOLE").value(40));
    }

    @Test
    void subscribeToUpdates_shouldReturnSseEmitter() throws Exception {
        // Given
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        when(dashboardService.subscribe()).thenReturn(emitter);

        // When/Then
        mockMvc.perform(get("/api/v1/dashboard/stream")
                        .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(status().isOk());
    }
}
