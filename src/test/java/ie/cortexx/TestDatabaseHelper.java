package ie.cortexx;

import ie.cortexx.util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class TestDatabaseHelper {

    public static final String PRODUCT_OK = "TEST-OK-001";
    public static final String PRODUCT_ZERO = "TEST-ZERO-001";
    public static final String PRODUCT_LOW = "TEST-LOW-001";
    public static final String PRODUCT_INACTIVE = "TEST-INACTIVE-001";

    public static Connection getConnection() throws SQLException {
        return DBConnection.getTestConnection();
    }

    public static void useTestDatabase() {
        DBConnection.useTestDatabase();
    }

    public static void useMainDatabase() {
        DBConnection.useMainDatabase();
    }

    public static void cleanTestData() throws SQLException {
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.execute("SET FOREIGN_KEY_CHECKS = 0");
            s.execute("TRUNCATE TABLE stock");
            s.execute("TRUNCATE TABLE products");
            s.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    public static void seedTestData() throws SQLException {
        cleanTestData();

        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate("""
                INSERT INTO products
                (sa_product_id, name, cost_price, markup_rate, vat_rate, category, is_active, last_synced)
                VALUES
                ('TEST-OK-001', 'Seed Product OK', 1.00, 0.2500, 0.2000, 'Test', TRUE, NOW()),
                ('TEST-ZERO-001', 'Seed Product Zero', 2.00, 0.3000, 0.2000, 'Test', TRUE, NOW()),
                ('TEST-LOW-001', 'Seed Product Low', 3.00, 0.3500, 0.2000, 'Test', TRUE, NOW()),
                ('TEST-INACTIVE-001', 'Seed Product Inactive', 4.00, 0.2000, 0.2000, 'Test', FALSE, NOW())
                """);

            s.executeUpdate("""
                INSERT INTO stock (product_id, quantity, reorder_level, last_updated)
                SELECT product_id, 10, 5, NOW() FROM products WHERE sa_product_id = 'TEST-OK-001'
                """);

            s.executeUpdate("""
                INSERT INTO stock (product_id, quantity, reorder_level, last_updated)
                SELECT product_id, 0, 5, NOW() FROM products WHERE sa_product_id = 'TEST-ZERO-001'
                """);

            s.executeUpdate("""
                INSERT INTO stock (product_id, quantity, reorder_level, last_updated)
                SELECT product_id, 2, 5, NOW() FROM products WHERE sa_product_id = 'TEST-LOW-001'
                """);

            s.executeUpdate("""
                INSERT INTO stock (product_id, quantity, reorder_level, last_updated)
                SELECT product_id, 7, 5, NOW() FROM products WHERE sa_product_id = 'TEST-INACTIVE-001'
                """);
        }
    }
}
