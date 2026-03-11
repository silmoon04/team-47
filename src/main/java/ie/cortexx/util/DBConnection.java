package ie.cortexx.util;

import java.sql.Connection;
import java.sql.SQLException;

// database connection factory
// reads creds from src/main/resources/db.properties
//
// usage:
//   try (Connection c = DBConnection.getConnection()) { ... }
//
// setup: copy db.properties.example to db.properties, fill in your password
public class DBConnection {

    // TODO: load db.properties from classpath using Properties + getResourceAsStream
    //   - store url, user, password
    //   - throw RuntimeException if file not found

    // TODO: return a new connection using DriverManager.getConnection(url, user, pw)
    public static Connection getConnection() throws SQLException {
        return null;
    }

    // TODO: same but using db.test.url for test database
    public static Connection getTestConnection() throws SQLException {
        return null;
    }
}