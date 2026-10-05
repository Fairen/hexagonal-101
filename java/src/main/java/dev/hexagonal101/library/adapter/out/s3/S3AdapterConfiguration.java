package dev.hexagonal101.library.adapter.out.s3;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import tools.jackson.databind.json.JsonMapper;

/** Switched on by {@code library.adapters.outbound=s3} in application.yml. */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "library.adapters", name = "outbound", havingValue = "s3")
@EnableConfigurationProperties(S3Properties.class)
public class S3AdapterConfiguration {

    @Bean
    S3Client libraryS3Client(S3Properties properties) {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(properties.region()))
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED);
        if (properties.endpoint() != null) {
            builder.endpointOverride(properties.endpoint()).forcePathStyle(true);
        }
        if (properties.accessKey() != null && properties.secretKey() != null) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(properties.accessKey(), properties.secretKey())));
        }
        return builder.build();
    }

    @Bean
    S3LibraryDocument s3LibraryDocument(S3Client s3, S3Properties properties, JsonMapper json) throws IOException {
        S3LibraryDocument document = new S3LibraryDocument(s3, json, properties.bucket(), properties.key());
        if (properties.seedIfMissing()) {
            try (InputStream seed = new ClassPathResource("s3/library-seed.json").getInputStream()) {
                document.createIfMissing(seed.readAllBytes());
            }
        }
        return document;
    }

    @Bean
    S3BookCatalog s3BookCatalog(S3LibraryDocument document) {
        return new S3BookCatalog(document);
    }

    @Bean
    S3MemberDirectory s3MemberDirectory(S3LibraryDocument document) {
        return new S3MemberDirectory(document);
    }

    @Bean
    S3LoanRepository s3LoanRepository(S3LibraryDocument document) {
        return new S3LoanRepository(document);
    }
}
