/**
 * The composition root: where the hexagon is assembled.
 * <p>
 * This is the only place that knows the concrete classes of every brick. Swapping an adapter
 * means changing a property in application.yml, nothing else.
 */
@NullMarked
package dev.hexagonal101.library.bootstrap;

import org.jspecify.annotations.NullMarked;
