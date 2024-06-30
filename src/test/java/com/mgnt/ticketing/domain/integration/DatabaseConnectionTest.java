package com.mgnt.ticketing.domain.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    public void testDatabaseConnection() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            assertThat(conn).isNotNull();
            assertThat(conn.isValid(1)).isTrue();
        }
    }
}