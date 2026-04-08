package ie.cortexx.iposca;

import ie.cortexx.util.DBConnection;
import org.junit.jupiter.api.Test;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;

// db schema + connection integration tests
//
// run with:  mvn verify  or  mvn test -Dtest=DBTestIT
// prereq:    mysql running, db/01_schema.sql executed
public class DBTestIT {

    static final String[] TABLES = {
        "system_config", "users", "merchant_details", "products", "stock",
        "discount_tiers", "customers", "sales", "sale_items", "payments",
        "orders", "online_orders", "online_order_items", "order_items", "statements", "reminders", "templates"
    };

    // helper: query information_schema and return first column of first row
    String schemaInfo(Connection c, String sql) throws Exception {
        try (var rs = c.createStatement().executeQuery(sql)) {
            return rs.next() ? rs.getString(1) : null;
        }
    }

    String activeSchema() {
        return DBConnection.isUsingTestDatabase() ? "iposca_test" : "iposca_database";
    }

    // ---- connection ----

    @Test void connects() throws Exception {
        try (var c = DBConnection.getConnection()) {
            assertNotNull(c);
            assertFalse(c.isClosed());
            System.out.println("[ok] connected");
        }
    }

    @Test void correctDb() throws Exception {
        String expected = activeSchema();
        try (var c = DBConnection.getConnection();
             var rs = c.createStatement().executeQuery("SELECT DATABASE()")) {
            rs.next();
            assertEquals(expected, rs.getString(1));
            System.out.println("[ok] database = " + expected);
        }
    }

    // ---- schema structure ----

    @Test void allTablesExist() throws Exception {
        String schema = activeSchema();
        try (var c = DBConnection.getConnection()) {
            var meta = c.getMetaData();
            for (String t : TABLES) {
                try (var rs = meta.getTables(null, schema, t, null)) {
                    assertTrue(rs.next(), "missing table: " + t);
                }
            }
            System.out.println("[ok] all tables exist");
        }
    }

    @Test void columnFixes() throws Exception {
        String schema = activeSchema();
        try (var c = DBConnection.getConnection()) {
            String base = "SELECT %s FROM information_schema.columns "
                + "WHERE table_schema='" + schema + "' AND table_name='%s' AND column_name='%s'";

            // merchant_details.merchant_id not auto_increment (THE bug that broke everything)
            assertFalse(schemaInfo(c, String.format(base, "EXTRA", "merchant_details", "merchant_id"))
                .contains("auto_increment"));

            // payments.payment_type is enum (was swapped with card_type, causes silent wrong data)
            assertTrue(schemaInfo(c, String.format(base, "COLUMN_TYPE", "payments", "payment_type"))
                .startsWith("enum"));

            // nullable fields that were NOT NULL (broke cash payments, new orders, unsent reminders)
            for (String[] col : new String[][]{
                {"payments", "card_type"}, {"orders", "delivered_at"}, {"reminders", "sent_at"}
            }) {
                assertEquals("YES", schemaInfo(c, String.format(base, "IS_NULLABLE", col[0], col[1])),
                    col[0] + "." + col[1] + " should be nullable");
            }

            // customers reminder columns split (was single reminder_dates + statuses)
            var meta = c.getMetaData();
            for (String col : new String[]{"date_1st_reminder", "status_1st_reminder",
                                           "date_2nd_reminder", "status_2nd_reminder"}) {
                try (var rs = meta.getColumns(null, schema, "customers", col)) {
                    assertTrue(rs.next(), "customers." + col + " missing");
                }
            }
            System.out.println("[ok] column fixes verified");
        }
    }

    // ---- cascades (worth 2 marks in demo) ----

    @Test void cascadeDeletes() throws Exception {
        String[] fks = {"fk_sale_items_sale", "fk_order_items_order", "fk_payments_sale"};
        String schema = activeSchema();
        try (var c = DBConnection.getConnection()) {
            for (String fk : fks) {
                var rs = c.createStatement().executeQuery(
                    "SELECT DELETE_RULE FROM information_schema.referential_constraints "
                    + "WHERE constraint_schema='" + schema + "' AND constraint_name='" + fk + "'");
                assertTrue(rs.next(), fk + " not found");
                assertEquals("CASCADE", rs.getString(1), fk + " should cascade");
                rs.close();
            }
            System.out.println("[ok] cascade deletes verified");
        }
    }

    // ---- constraints (shows assertThrows pattern for the team) ----

    @Test void rejectsNegativeStock() throws Exception {
        // CHECK (quantity >= 0) on stock table
        try (var c = DBConnection.getConnection();
             var rs = c.createStatement().executeQuery("SELECT product_id FROM stock LIMIT 1")) {
            if (!rs.next()) { System.out.println("[skip] no stock data"); return; }
            var ps = c.prepareStatement("UPDATE stock SET quantity = -1 WHERE product_id = ?");
            ps.setInt(1, rs.getInt(1));
            assertThrows(SQLException.class, ps::executeUpdate);
            System.out.println("[ok] negative stock rejected");
        }
    }

    @Test void rejectsDuplicateUsername() throws Exception {
        // UNIQUE constraint on users.username
        try (var c = DBConnection.getConnection();
             var rs = c.createStatement().executeQuery("SELECT username FROM users LIMIT 1")) {
            if (!rs.next()) { System.out.println("[skip] no users"); return; }
            var ps = c.prepareStatement(
                "INSERT INTO users (username,password_hash,full_name,role,merchant_id) VALUES (?,'x','x','ADMIN',1)");
            ps.setString(1, rs.getString(1));
            assertThrows(SQLException.class, ps::executeUpdate);
            System.out.println("[ok] duplicate username rejected");
        }
    }

    // ---- data integrity (needs demo/test data loaded) ----

    @Test void orderTotalsMatchItems() throws Exception {
        // checks that order header totals = sum of (qty * unit_price) in items
        // shows JOIN + GROUP BY + HAVING pattern
        try (var c = DBConnection.getConnection();
             var rs = c.createStatement().executeQuery(
                 "SELECT o.order_id FROM orders o "
                 + "JOIN order_items oi ON o.order_id=oi.order_id "
                 + "GROUP BY o.order_id, o.total_amount "
                 + "HAVING o.total_amount != SUM(oi.quantity * oi.unit_price)")) {
            assertFalse(rs.next(), "order total mismatch found");
            System.out.println("[ok] order totals match items");
        }
    }

    @Test void onlineOrderTotalsMatchItems() throws Exception {
        try (var c = DBConnection.getConnection();
             var rs = c.createStatement().executeQuery(
                 "SELECT o.online_order_id FROM online_orders o "
                 + "JOIN online_order_items oi ON o.online_order_id = oi.online_order_id "
                 + "GROUP BY o.online_order_id, o.total_price, o.discount_applied "
                 + "HAVING o.total_price != SUM(oi.quantity * oi.unit_price) - COALESCE(o.discount_applied, 0)")) {
            assertFalse(rs.next(), "online order total mismatch found");
            System.out.println("[ok] online order totals match items");
        }
    }

    // ---- summary (prints row counts so you can eyeball the data) ----

    @Test void schemaSummary() throws Exception {
        try (var c = DBConnection.getConnection()) {
            System.out.println("\n=== SCHEMA SUMMARY ===");
            for (String t : TABLES) {
                var rs = c.createStatement().executeQuery("SELECT COUNT(*) FROM " + t);
                rs.next();
                System.out.printf("  %-20s %d rows%n", t, rs.getInt(1));
                rs.close();
            }
            System.out.println("======================\n");
        }
    }
}
