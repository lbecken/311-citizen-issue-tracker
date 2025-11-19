package gov.lby.cityissuetracker.kafka;

import gov.lby.cityissuetracker.kafka.event.IssueEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.time.Duration;

@Configuration
@EnableKafkaStreams
@Slf4j
public class DashboardMetricsProcessor {

    @Bean
    public KStream<String, IssueEvent> processIssueEvents(StreamsBuilder builder) {
        JsonSerde<IssueEvent> issueEventSerde = new JsonSerde<>(IssueEvent.class);
        issueEventSerde.configure(
                java.util.Map.of("spring.json.trusted.packages", "gov.lby.cityissuetracker.kafka.event"),
                false
        );

        KStream<String, IssueEvent> stream = builder.stream(
                KafkaTopicConfig.ISSUE_EVENTS_TOPIC,
                Consumed.with(Serdes.String(), issueEventSerde)
        );

        // Count events by status in 5-minute windows
        stream.groupBy((key, event) -> event.getStatus().name())
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(5)))
                .count(Materialized.as("status-counts"))
                .toStream()
                .peek((key, count) -> log.debug("Status {} count in window: {}", key.key(), count));

        // Count events by category in 5-minute windows
        stream.groupBy((key, event) -> event.getCategory().name())
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(5)))
                .count(Materialized.as("category-counts"))
                .toStream()
                .peek((key, count) -> log.debug("Category {} count in window: {}", key.key(), count));

        // Count created events in 5-minute windows
        stream.filter((key, event) -> event.getEventType() == IssueEvent.EventType.CREATED)
                .groupBy((key, event) -> "created")
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(5)))
                .count(Materialized.as("created-counts"))
                .toStream()
                .peek((key, count) -> log.debug("Created count in window: {}", count));

        // Count resolved events in 5-minute windows
        stream.filter((key, event) -> event.getEventType() == IssueEvent.EventType.RESOLVED)
                .groupBy((key, event) -> "resolved")
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(5)))
                .count(Materialized.as("resolved-counts"))
                .toStream()
                .peek((key, count) -> log.debug("Resolved count in window: {}", count));

        return stream;
    }
}
