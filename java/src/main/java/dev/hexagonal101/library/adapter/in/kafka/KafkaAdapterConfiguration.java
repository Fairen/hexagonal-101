package dev.hexagonal101.library.adapter.in.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.config.TopicBuilder;

/** Switched on by {@code library.adapters.inbound=kafka} in application.yml. */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "library.adapters", name = "inbound", havingValue = "kafka")
@Import(LoanRequestListener.class)
public class KafkaAdapterConfiguration {

    @Bean
    NewTopic loanRequestsTopic() {
        return TopicBuilder.name(LoanRequestListener.REQUEST_TOPIC).build();
    }

    @Bean
    NewTopic loanResultsTopic() {
        return TopicBuilder.name(LoanRequestListener.RESULT_TOPIC).build();
    }
}
