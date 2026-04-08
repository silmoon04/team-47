package ie.cortexx;

import ie.cortexx.util.DBConnection;

import java.math.BigDecimal;
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
            s.executeUpdate("""
                DELETE s FROM stock s
                JOIN products p ON p.product_id = s.product_id
                WHERE p.sa_product_id IN ('TEST-OK-001', 'TEST-ZERO-001', 'TEST-LOW-001', 'TEST-INACTIVE-001')
                """);
            s.executeUpdate("""
                DELETE FROM products
                WHERE sa_product_id IN ('TEST-OK-001', 'TEST-ZERO-001', 'TEST-LOW-001', 'TEST-INACTIVE-001')
                """);
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
                INSERT INTO stock (product_id, quantity, reorder_level)
                SELECT product_id, 10, 5 FROM products WHERE sa_product_id = 'TEST-OK-001'
                """);

            s.executeUpdate("""
                INSERT INTO stock (product_id, quantity, reorder_level)
                SELECT product_id, 0, 5 FROM products WHERE sa_product_id = 'TEST-ZERO-001'
                """);

            s.executeUpdate("""
                INSERT INTO stock (product_id, quantity, reorder_level)
                SELECT product_id, 2, 5 FROM products WHERE sa_product_id = 'TEST-LOW-001'
                """);

            s.executeUpdate("""
                INSERT INTO stock (product_id, quantity, reorder_level)
                SELECT product_id, 7, 5 FROM products WHERE sa_product_id = 'TEST-INACTIVE-001'
                """);
        }
    }

    // -- shared test helpers --

    public static int id(String sql) throws Exception {
        try (var c = DBConnection.getConnection();
             var rs = c.createStatement().executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public static String str(String sql) throws Exception {
        try (var c = DBConnection.getConnection();
             var rs = c.createStatement().executeQuery(sql)) {
            return rs.next() ? rs.getString(1) : null;
        }
    }

    public static String str(String table, String col, String idCol, int id) throws Exception {
        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement("SELECT " + col + " FROM " + table + " WHERE " + idCol + " = ?")) {
            ps.setInt(1, id);
            var rs = ps.executeQuery();
            return rs.next() ? rs.getString(1) : null;
        }
    }

    public static void del(String table, String idCol, int id) throws Exception {
        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement("DELETE FROM " + table + " WHERE " + idCol + " = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    public static int count(String table, String idCol, int id) throws Exception {
        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement("SELECT COUNT(*) FROM " + table + " WHERE " + idCol + " = ?")) {
            ps.setInt(1, id);
            try (var rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
