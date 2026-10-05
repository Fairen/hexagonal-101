/**
 * The application layer: the use cases of the library and the ports they need.
 * <p>
 * {@code port.in} is what the outside world can ask the hexagon to do.
 * {@code port.out} is what the hexagon needs from the outside world.
 * Both are plain Java interfaces owned by the application, never by the adapters.
 */
@NullMarked
package dev.hexagonal101.library.application;

import org.jspecify.annotations.NullMarked;
