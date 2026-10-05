package dev.hexagonal101.library.bootstrap;

import java.time.Clock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import dev.hexagonal101.library.adapter.in.kafka.KafkaAdapterConfiguration;
import dev.hexagonal101.library.adapter.in.rest.RestAdapterConfiguration;
import dev.hexagonal101.library.adapter.out.s3.S3AdapterConfiguration;
import dev.hexagonal101.library.adapter.out.sqlite.SqliteAdapterConfiguration;
import dev.hexagonal101.library.application.service.BorrowBookService;
import dev.hexagonal101.library.domain.LoanPolicy;

/**
 * The composition root (Cockburn's "configurator"): it lists the pieces, it contains no logic.
 * <p>
 * The hexagon's classes are plain Java with no Spring annotation: they are listed here and
 * Spring builds them through their constructor. The adapters are all listed too; each one
 * switches itself on or off from {@code library.adapters.*} in application.yml, so exactly
 * one driving and one driven adapter start, and the service receives whichever is plugged in.
 */
@SpringBootApplication
@Import({
        // The hexagon
        BorrowBookService.class,
        LoanPolicy.class,
        // The adapters
        RestAdapterConfiguration.class,
        KafkaAdapterConfiguration.class,
        S3AdapterConfiguration.class,
        SqliteAdapterConfiguration.class
})
public class LibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
    }

    /** The system clock. Tests of the use case plug in a fixed one instead. */
    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }
}
