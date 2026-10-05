package dev.hexagonal101.library.adapter.out.s3;

import java.net.URI;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * The {@code library.s3.*} settings of application.yml.
 *
 * @param endpoint      leave empty for AWS, set it for an S3-compatible store
 * @param accessKey     leave empty to use the default AWS credentials chain
 * @param seedIfMissing create the bucket and a sample library.json on startup if they do not exist
 */
@ConfigurationProperties("library.s3")
public record S3Properties(
        @Nullable URI endpoint,
        @DefaultValue("us-east-1") String region,
        @DefaultValue("library") String bucket,
        @DefaultValue("library.json") String key,
        @Nullable String accessKey,
        @Nullable String secretKey,
        @DefaultValue("true") boolean seedIfMissing) {
}
