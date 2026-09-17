package com.lab.borrow.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSchemaUpdater implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaUpdater(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!hasColumn("users", "password_hash")) {
            jdbcTemplate.execute(
                    "ALTER TABLE users ADD COLUMN password_hash TEXT"
            );
        }
    }

    private boolean hasColumn(String tableName, String columnName) {
        return Boolean.TRUE.equals(jdbcTemplate.query(
                "PRAGMA table_info(" + tableName + ")",
                resultSet -> {
                    while (resultSet.next()) {
                        if (columnName.equals(resultSet.getString("name"))) {
                            return true;
                        }
                    }
                    return false;
                }
        ));
    }
}
