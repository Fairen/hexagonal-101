package dev.hexagonal101.library.adapter.out.sqlite;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** The {@code library.sqlite.*} settings of application.yml. */
@ConfigurationProperties("library.sqlite")
public record SqliteProperties(@DefaultValue("jdbc:sqlite:library.db") String url) {
}
