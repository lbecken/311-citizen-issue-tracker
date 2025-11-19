package gov.lby.cityissuetracker.controller;

import gov.lby.cityissuetracker.kafka.event.DashboardMetrics;
import gov.lby.cityissuetracker.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard metrics and real-time updates")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/metrics")
    @Operation(summary = "Get current dashboard metrics", description = "Returns aggregated metrics for all issues")
    public DashboardMetrics getMetrics() {
        return dashboardService.getMetrics();
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Subscribe to real-time updates", description = "Server-Sent Events stream for real-time dashboard updates")
    public SseEmitter subscribeToUpdates() {
        return dashboardService.subscribe();
    }
}
