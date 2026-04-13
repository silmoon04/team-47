package ie.cortexx.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class DBConnection {

    @FunctionalInterface
    public interface TransactionWork<T> {
        T run(Connection connection) throws SQLException;
    }

    @FunctionalInterface
    public interface TransactionRunner {
        <T> T execute(TransactionWork<T> work) throws SQLException;
    }

    private static String url;
    private static String user;
    private static String password;
    private static String testUrl;
    private static volatile boolean useTestDatabase = Boolean.getBoolean("db.use.test");

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

    private static volatile Connection preWarmedConnection;

    public static Connection getConnection() throws SQLException {
        Connection cached = preWarmedConnection;
        if (cached != null) {
            preWarmedConnection = null;
            if (!cached.isClosed()) {
                return cached;
            }
        }
        String activeUrl = isUsingTestDatabase() ? testUrl : url;
        return DriverManager.getConnection(activeUrl, user, password);
    }

    public static void warmUp() {
        new Thread(() -> {
            try {
                preWarmedConnection = DriverManager.getConnection(url, user, password);
            } catch (SQLException ignored) { }
        }, "db-warmup").start();
    }

    public static Connection getTestConnection() throws SQLException {
        return DriverManager.getConnection(testUrl, user, password);
    }

    public static <T> T withTransaction(TransactionWork<T> work) throws SQLException {
        try (Connection connection = getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                T result = work.run(connection);
                connection.commit();
                return result;
            } catch (SQLException | RuntimeException error) {
                connection.rollback();
                throw error;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        }
    }

    public static boolean isUsingTestDatabase() {
        return useTestDatabase || Boolean.getBoolean("db.use.test");
    }
}
