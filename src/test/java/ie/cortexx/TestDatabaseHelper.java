package ie.cortexx;

import ie.cortexx.util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

// utility for integration tests that need a real database
public class TestDatabaseHelper {

    public static Connection getConnection() throws SQLException {
        return DBConnection.getTestConnection();
    }

    public static void cleanTestData() throws SQLException {
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.execute("SET FOREIGN_KEY_CHECKS = 0");
            s.execute("TRUNCATE TABLE stock");
            s.execute("TRUNCATE TABLE products");
            s.execute("TRUNCATE TABLE users");
            s.execute("TRUNCATE TABLE system_config");
            s.execute("DELETE FROM merchant_details");
            s.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    public static void seedTestData() throws SQLException {
        cleanTestData();

        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate("""
                INSERT INTO merchant_details
                (merchant_id, business_name, address, phone, email, sa_merchant_id)
                VALUES (1, 'Test Pharmacy', '1 Test Street', '02070000000', 'test@test.local', 1001)
                """);

            s.executeUpdate("""
                INSERT INTO system_config (config_key, config_value, updated_at)
                VALUES (1, 20.00, NOW())
                """);

            s.executeUpdate("""
                INSERT INTO users (username, password_hash, full_name, email, phone, role, merchant_id)
                VALUES ('admin', 'test_hash', 'Test Admin', 'admin@test.local', '02070000001', 'ADMIN', 1)
                """);

            s.executeUpdate("""
                INSERT INTO products
                (sa_product_id, name, cost_price, markup_rate, vat_rate, category, is_active, last_synced)
                VALUES
                ('T-9001', 'Test Product A', 1.00, 0.25, 0.20, 'Test', TRUE, NOW()),
                ('T-9002', 'Test Product B', 2.00, 0.30, 0.20, 'Test', TRUE, NOW())
                """);

            s.executeUpdate("""
                INSERT INTO stock (product_id, quantity, reorder_level, last_updated)
                VALUES
                (1, 10, 5, NOW()),
                (2, 2, 5, NOW())
                """);
        }
    }
}
