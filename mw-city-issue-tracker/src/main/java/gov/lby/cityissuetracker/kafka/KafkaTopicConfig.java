package gov.lby.cityissuetracker.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String ISSUE_EVENTS_TOPIC = "issue.events";
    public static final String DASHBOARD_UPDATES_TOPIC = "dashboard.updates";
    public static final String METRICS_STREAM_TOPIC = "metrics.stream";

    @Bean
    public NewTopic issueEventsTopic() {
        return TopicBuilder.name(ISSUE_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic dashboardUpdatesTopic() {
        return TopicBuilder.name(DASHBOARD_UPDATES_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic metricsStreamTopic() {
        return TopicBuilder.name(METRICS_STREAM_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
