/**
 * Driving adapter: exposes the borrow use case over Kafka.
 * <p>
 * Same role as the REST controller, different transport: a message in, a command to the port,
 * a result message out on a reply topic.
 */
@NullMarked
package dev.hexagonal101.library.adapter.in.kafka;

import org.jspecify.annotations.NullMarked;
