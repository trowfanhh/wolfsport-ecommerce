package com.wolfsport.wolfsport;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DatabaseTestController {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseTestController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/test-db")
    public String testDatabase() {
        String database = jdbcTemplate.queryForObject(
                "SELECT DATABASE()", String.class);

        return "Connected to database: " + database;
    }
}