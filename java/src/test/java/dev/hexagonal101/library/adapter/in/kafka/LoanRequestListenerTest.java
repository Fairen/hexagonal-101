package dev.hexagonal101.library.adapter.in.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import dev.hexagonal101.library.application.port.in.BorrowBookResult;
import dev.hexagonal101.library.application.port.in.BorrowBookUseCase;

/** Round trip through a real Kafka broker; the use case behind the port is a mock. */
@SpringBootTest(properties = {
        "library.adapters.inbound=kafka",
        "spring.kafka.consumer.auto-offset-reset=earliest"
})
@Testcontainers
class LoanRequestListenerTest {

    @Container
    @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer("apache/kafka-native:4.1.0");

    @Autowired
    private KafkaTemplate<String, String> template;

    @MockitoBean
    private BorrowBookUseCase borrowBook;

    @Test
    void publishes_the_outcome_on_the_result_topic() throws Exception {
        given(borrowBook.borrow(any())).willReturn(new BorrowBookResult.Granted(
                "l-1", "m-1", "b-1", LocalDate.of(2026, 10, 2), LocalDate.of(2026, 10, 16)));

        try (KafkaConsumer<String, String> results = resultConsumer()) {
            template.send(LoanRequestListener.REQUEST_TOPIC,
                    "{\"requestId\":\"r-42\",\"memberId\":\"m-1\",\"bookId\":\"b-1\"}").get();

            String result = pollOne(results);
            assertThat(result)
                    .contains("\"requestId\":\"r-42\"")
                    .contains("\"status\":\"GRANTED\"")
                    .contains("\"dueOn\":\"2026-10-16\"");
        }
    }

    private KafkaConsumer<String, String> resultConsumer() {
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-results",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"),
                new StringDeserializer(), new StringDeserializer());
        consumer.subscribe(List.of(LoanRequestListener.RESULT_TOPIC));
        return consumer;
    }

    private static String pollOne(KafkaConsumer<String, String> consumer) {
        long deadline = System.currentTimeMillis() + 30_000;
        while (System.currentTimeMillis() < deadline) {
            for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(500))) {
                return record.value();
            }
        }
        throw new AssertionError("No result message within 30 seconds");
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(KafkaAdapterConfiguration.class)
    static class KafkaAdapterTestApplication {
    }
}
