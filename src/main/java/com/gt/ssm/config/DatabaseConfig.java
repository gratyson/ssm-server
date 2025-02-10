package com.gt.ssm.config;

import com.zaxxer.hikari.HikariConfig;
import org.postgresql.ds.PGSimpleDataSource;
import org.postgresql.osgi.PGDataSourceFactory;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class DatabaseConfig extends HikariConfig {

    @Bean
    public DataSource getDataSource(@Value("${ssm.datasource.postgres.url}") String url,
                                    @Value("${ssm.datasource.postgres.username}") String username,
                                    @Value("${ssm.datasource.postgres.password}") String password) {
        LoggerFactory.getLogger(DatabaseConfig.class).warn("URL: " + url);

        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(url);
        dataSource.setUser(username);
        dataSource.setPassword(password);

        return dataSource;
    }

    @Bean
    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate(DataSource dataSource) {

        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean
    public Connection getBlobDatabaseConnection(@Value("${ssm.datasource.postgres.url}") String url,
                                                @Value("${ssm.datasource.postgres.username}") String username,
                                                @Value("${ssm.datasource.postgres.password}") String password) throws SQLException {
        LoggerFactory.getLogger(DatabaseConfig.class).warn("URL: " + url);

        Properties props = new Properties();
        props.setProperty("user", username);
        props.setProperty("password", password);

        Connection conn = DriverManager.getConnection(url, props);
        conn.setAutoCommit(false);
        conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

        return conn;
    }
}
