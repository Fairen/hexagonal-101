/**
 * Driving adapter: exposes the borrow use case over HTTP.
 * <p>
 * Its whole job is translation: JSON in, command to the port, decision out, HTTP status back.
 * No business rule lives here.
 */
@NullMarked
package dev.hexagonal101.library.adapter.in.rest;

import org.jspecify.annotations.NullMarked;
