package ie.cortexx.iposca;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DBTestIT {

    @Test
    void testProjectDatabaseConnectionAfterReset() throws Exception {
        try (Connection conn = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/ipos_ca",
            "root",
            "password123")) {

            assertNotNull(conn);

            String resetSql = Files.readString(Path.of("db/reset_demo.sql"));

            try (Statement stmt = conn.createStatement()) {
                for (String sql : resetSql.split(";")) {
                    String trimmed = sql.trim();
                    if (!trimmed.isEmpty()) {
                        stmt.execute(trimmed);
                    }
                }
            }
        }
    }
}
