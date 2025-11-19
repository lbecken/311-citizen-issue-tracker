package gov.lby.cityissuetracker.kafka;

import gov.lby.cityissuetracker.kafka.event.IssueEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, IssueEvent> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "dashboard-sse-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JsonDeserializer<IssueEvent> deserializer = new JsonDeserializer<>(IssueEvent.class);
        deserializer.addTrustedPackages("gov.lby.cityissuetracker.kafka.event");
        deserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, IssueEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, IssueEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
