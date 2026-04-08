package ie.cortexx.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class DBConnection {

    private static String url;
    private static String user;
    private static String password;
    private static String testUrl;
    private static boolean useTestDatabase = Boolean.getBoolean("db.use.test");

    static {
        try (InputStream in = DBConnection.class.getClassLoader()
            .getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new RuntimeException("db.properties not found in classpath. create src/main/resources/db.properties");
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

    public static void useTestDatabase() {
        useTestDatabase = true;
    }

    public static void useMainDatabase() {
        useTestDatabase = false;
    }

    public static Connection getConnection() throws SQLException {
        String activeUrl = isUsingTestDatabase() ? testUrl : url;
        return DriverManager.getConnection(activeUrl, user, password);
    }

    public static Connection getTestConnection() throws SQLException {
        return DriverManager.getConnection(testUrl, user, password);
    }

    public static boolean isUsingTestDatabase() {
        return useTestDatabase || Boolean.getBoolean("db.use.test");
    }
}
