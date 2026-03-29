package ie.cortexx.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

// database connection factory

// reads creds from src/main/resources/db.properties
// usage: try (Connection c = DBConnection.getConnection()) { ... }

// basically
//   1. static block loads db.properties from classpath on first use
//   2. stores url, user, password as static fields
//   3. getConnection() calls DriverManager.getConnection(url, user, pw)
//   4. every caller must close the connection (use try-with-resources)

public class DBConnection {

    private static String url;
    private static String user;
    private static String password;
    private static String testUrl;

    // loads once when class is first referenced
    static {
        try (InputStream in = DBConnection.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new RuntimeException("db.properties not found in classpath. "
                    + "create src/main/resources/db.properties");
            }
            Properties props = new Properties();
            props.load(in);
            url = props.getProperty("db.url");
            user = props.getProperty("db.user");
            password = props.getProperty("db.password");
            testUrl = props.getProperty("db.test.url");
        } catch (IOException e) {
            throw new RuntimeException("failed to load db.properties", e);
        }
    }

    // returns a new connection to iposca_database
    // caller MUST close it (use try-with-resources)
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    // same but for test db (iposca_test)
    public static Connection getTestConnection() throws SQLException {
        return DriverManager.getConnection(testUrl, user, password);
    }
}
