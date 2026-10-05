package dev.hexagonal101.library.adapter.in.rest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/** Switched on by {@code library.adapters.inbound=rest} in application.yml. */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "library.adapters", name = "inbound", havingValue = "rest")
@Import({LoanController.class, RestExceptionHandler.class})
public class RestAdapterConfiguration {
}
