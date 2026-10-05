package dev.hexagonal101.library.adapter.out.sqlite;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.sqlite.SQLiteDataSource;

/**
 * Switched on by {@code library.adapters.outbound=sqlite} in application.yml.
 * <p>
 * The adapter owns its infrastructure: data source, schema migrations, SQL client.
 * Nothing database-related is created when the S3 adapter is selected instead.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "library.adapters", name = "outbound", havingValue = "sqlite")
@EnableConfigurationProperties(SqliteProperties.class)
public class SqliteAdapterConfiguration {

    @Bean
    DataSource libraryDataSource(SqliteProperties properties) {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(properties.url());
        return dataSource;
    }

    /** Creates the schema and the sample data (src/main/resources/db/sqlite) on startup. */
    @Bean(initMethod = "migrate")
    Flyway libraryFlyway(DataSource libraryDataSource) {
        return Flyway.configure()
                .dataSource(libraryDataSource)
                .locations("classpath:db/sqlite")
                .load();
    }

    @Bean
    @DependsOn("libraryFlyway")
    JdbcClient libraryJdbcClient(DataSource libraryDataSource) {
        return JdbcClient.create(libraryDataSource);
    }

    @Bean
    SqliteBookCatalog sqliteBookCatalog(JdbcClient libraryJdbcClient) {
        return new SqliteBookCatalog(libraryJdbcClient);
    }

    @Bean
    SqliteMemberDirectory sqliteMemberDirectory(JdbcClient libraryJdbcClient) {
        return new SqliteMemberDirectory(libraryJdbcClient);
    }

    @Bean
    SqliteLoanRepository sqliteLoanRepository(JdbcClient libraryJdbcClient) {
        return new SqliteLoanRepository(libraryJdbcClient);
    }
}
