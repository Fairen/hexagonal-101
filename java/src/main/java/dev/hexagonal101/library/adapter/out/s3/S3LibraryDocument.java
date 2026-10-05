package dev.hexagonal101.library.adapter.out.s3;

import java.util.function.UnaryOperator;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads and writes library.json in a bucket.
 * <p>
 * The whole library lives in one object, so every write is "read, modify, write back".
 * Two concurrent writers could overwrite each other. S3 conditional writes prevent that:
 * we only write back if the object still has the ETag we read ({@code If-Match}).
 * Otherwise someone else wrote in between: we read again and retry. This is optimistic locking.
 */
class S3LibraryDocument {

    private static final int MAX_ATTEMPTS = 5;

    private final S3Client s3;
    private final JsonMapper json;
    private final String bucket;
    private final String key;

    S3LibraryDocument(S3Client s3, JsonMapper json, String bucket, String key) {
        this.s3 = s3;
        this.json = json;
        this.bucket = bucket;
        this.key = key;
    }

    LibraryJson read() {
        return readVersioned().content();
    }

    void update(UnaryOperator<LibraryJson> change) {
        for (int attempt = 1; ; attempt++) {
            Versioned current = readVersioned();
            PutObjectRequest put = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType("application/json")
                    .ifMatch(current.eTag())
                    .build();
            try {
                s3.putObject(put, RequestBody.fromBytes(json.writeValueAsBytes(change.apply(current.content()))));
                return;
            } catch (S3Exception e) {
                if (!isConcurrentWrite(e) || attempt == MAX_ATTEMPTS) {
                    throw e;
                }
            }
        }
    }

    /** Creates the bucket and the document if needed. Never overwrites an existing document. */
    void createIfMissing(byte[] seed) {
        try {
            s3.headBucket(request -> request.bucket(bucket));
        } catch (NoSuchBucketException e) {
            s3.createBucket(request -> request.bucket(bucket));
        }
        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("application/json")
                .ifNoneMatch("*")
                .build();
        try {
            s3.putObject(put, RequestBody.fromBytes(seed));
        } catch (S3Exception e) {
            if (!isConcurrentWrite(e)) {
                throw e;
            }
        }
    }

    private Versioned readVersioned() {
        ResponseBytes<GetObjectResponse> object = s3.getObjectAsBytes(request -> request.bucket(bucket).key(key));
        return new Versioned(json.readValue(object.asByteArray(), LibraryJson.class), object.response().eTag());
    }

    /** 412: the ETag no longer matches. 409: another conditional write is in progress. */
    private static boolean isConcurrentWrite(S3Exception e) {
        return e.statusCode() == 412 || e.statusCode() == 409;
    }

    private record Versioned(LibraryJson content, String eTag) {
    }
}
