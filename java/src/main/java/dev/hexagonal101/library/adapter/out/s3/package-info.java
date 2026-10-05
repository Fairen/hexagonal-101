/**
 * Driven adapter: implements the persistence ports on top of a single JSON document in S3.
 * <p>
 * Everything S3-specific (buckets, ETags, JSON layout) stays in this package.
 * The application only sees {@code BookCatalog}, {@code MemberDirectory} and {@code LoanRepository}.
 */
@NullMarked
package dev.hexagonal101.library.adapter.out.s3;

import org.jspecify.annotations.NullMarked;
