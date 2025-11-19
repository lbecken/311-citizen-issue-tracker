package gov.lby.cityissuetracker.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String ISSUE_EXCHANGE = "issue.exchange";
    public static final String ISSUE_DLX = "issue.dlx";
    public static final String VALIDATION_QUEUE = "issue.validation";
    public static final String ASSIGNMENT_QUEUE = "issue.assignment";
    public static final String ISSUE_REPORTED_ROUTING_KEY = "issue.reported";
    public static final String ISSUE_VALIDATED_ROUTING_KEY = "issue.validated";

    @Bean
    public TopicExchange issueExchange() {
        return new TopicExchange(ISSUE_EXCHANGE);
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(ISSUE_DLX);
    }

    @Bean
    public Queue validationQueue() {
        return QueueBuilder.durable(VALIDATION_QUEUE)
                .withArgument("x-dead-letter-exchange", ISSUE_DLX)
                .build();
    }

    @Bean
    public Queue assignmentQueue() {
        return QueueBuilder.durable(ASSIGNMENT_QUEUE)
                .withArgument("x-dead-letter-exchange", ISSUE_DLX)
                .build();
    }

    @Bean
    public Binding validationBinding(TopicExchange issueExchange, Queue validationQueue) {
        return BindingBuilder.bind(validationQueue)
                .to(issueExchange)
                .with(ISSUE_REPORTED_ROUTING_KEY);
    }

    @Bean
    public Binding assignmentBinding(TopicExchange issueExchange, Queue assignmentQueue) {
        return BindingBuilder.bind(assignmentQueue)
                .to(issueExchange)
                .with(ISSUE_VALIDATED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
