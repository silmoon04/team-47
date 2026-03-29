package ie.cortexx.dao;

import ie.cortexx.enums.*;
import ie.cortexx.model.*;
import ie.cortexx.util.DBConnection;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;

// tests for the example DAO methods i (silmoon) wrote
// each test matches one working method in a DAO class
//
// prereq: mysql running, 01_schema.sql + demo data loaded
//
// how these tests work:
//   @Test marks a method as a test case, junit runs it automatically
//   assertNotNull(x)        = fail if x is null
//   assertNull(x)           = fail if x is NOT null
//   assertEquals(a, b)      = fail if a != b
//   assertTrue(condition)   = fail if condition is false
//   assertFalse(condition)  = fail if condition is true
//
// the "throws Exception" on each test just lets any sql errors
// bubble up as a test failure instead of needing try/catch everywhere

public class DAOExampleIT {

    // ---- UserDAO ----

    // calls authenticate() with the demo admin creds
    // checks we get a real User back with the right fields
    @Test void authenticateValidUser() throws Exception {
        var dao = new UserDAO();
        User u = dao.authenticate("sysdba", "masterkey");

        assertNotNull(u, "sysdba should exist in demo data");
        assertEquals("sysdba", u.getUsername());
        assertEquals(UserRole.ADMIN, u.getRole());
        assertTrue(u.isActive());
    }

    // wrong password should return null, not throw
    @Test void authenticateWrongPassword() throws Exception {
        var dao = new UserDAO();
        User u = dao.authenticate("sysdba", "wrongpassword");

        assertNull(u, "wrong password should return null");
    }

    // ---- StockDAO ----

    // sells 5 units, checks qty went down, then restores it
    @Test void updateQuantityDeductsStock() throws Exception {
        var dao = new StockDAO();

        // read current qty for product 1 (paracetamol)
        int before = getStockQty(1);

        // sell 5 units (negative delta = deduct)
        dao.updateQuantity(1, -5);

        // check it went down by 5
        assertEquals(before - 5, getStockQty(1));

        // put it back so we dont mess up other tests
        dao.updateQuantity(1, 5);
    }

    // checks that findLowStock only returns items at or below reorder level
    @Test void findLowStockReturnsCorrectItems() throws Exception {
        var dao = new StockDAO();
        var low = dao.findLowStock();

        assertFalse(low.isEmpty(), "demo data should have low stock items");

        // every item in the list should actually be low
        for (StockItem si : low) {
            assertTrue(si.getQuantity() <= si.getReorderLevel(),
                si.getProductName() + " qty=" + si.getQuantity()
                + " should be <= reorder=" + si.getReorderLevel());
        }
    }

    // ---- PaymentDAO ----

    // inserts a cash payment, checks it got an auto-generated id, then cleans up
    @Test void savePaymentCash() throws Exception {
        // grab any existing sale_id to attach the payment to
        int saleId = getFirstId("SELECT sale_id FROM sales LIMIT 1");
        if (saleId == 0) { System.out.println("[skip] no sales data"); return; }

        // build a cash payment (card fields left null)
        Payment p = new Payment();
        p.setSaleId(saleId);
        p.setPaymentType(PaymentType.CASH);
        p.setAmount(new BigDecimal("14.40"));
        p.setChangeGiven(new BigDecimal("0.60"));

        // save it
        new PaymentDAO().save(p);

        // save() should have set the generated id on the object
        assertTrue(p.getPaymentId() > 0, "should have generated payment_id");

        // cleanup so test data doesnt pile up
        deleteRow("payments", "payment_id", p.getPaymentId());
    }

    // ---- CustomerDAO ----

    // changes a customer to SUSPENDED, verifies, then restores original status
    @Test void updateAccountStatusWorks() throws Exception {
        // read first customer's current status so we can restore it after
        String[] row = getFirstRow(
            "SELECT customer_id, account_status FROM customers LIMIT 1");
        if (row == null) { System.out.println("[skip] no customers"); return; }
        int custId = Integer.parseInt(row[0]);
        String original = row[1];

        // change to SUSPENDED
        new CustomerDAO().updateAccountStatus(custId, AccountStatus.SUSPENDED);

        // verify it actually changed in the db
        assertEquals("SUSPENDED", getStringById("customers", "account_status", "customer_id", custId));

        // restore original so we dont break other tests
        new CustomerDAO().updateAccountStatus(custId, AccountStatus.valueOf(original));
    }

    // ---- OrderDAO ----

    // same pattern as customer status but for order status
    @Test void updateOrderStatusWorks() throws Exception {
        String[] row = getFirstRow(
            "SELECT order_id, order_status FROM orders LIMIT 1");
        if (row == null) { System.out.println("[skip] no orders"); return; }
        int orderId = Integer.parseInt(row[0]);
        String original = row[1];

        new OrderDAO().updateStatus(orderId, OrderStatus.DELIVERED);

        assertEquals("DELIVERED", getStringById("orders", "order_status", "order_id", orderId));

        // restore
        new OrderDAO().updateStatus(orderId, OrderStatus.valueOf(original));
    }

    // ---- helpers (keep tests short) ----

    // reads stock quantity for a product directly from db
    int getStockQty(int productId) throws Exception {
        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement("SELECT quantity FROM stock WHERE product_id = ?")) {
            ps.setInt(1, productId);
            var rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // reads first int from a query (e.g. "SELECT sale_id FROM sales LIMIT 1")
    int getFirstId(String sql) throws Exception {
        try (var c = DBConnection.getConnection();
             var rs = c.createStatement().executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // reads first row as string array (e.g. [customer_id, account_status])
    String[] getFirstRow(String sql) throws Exception {
        try (var c = DBConnection.getConnection();
             var rs = c.createStatement().executeQuery(sql)) {
            if (!rs.next()) return null;
            int cols = rs.getMetaData().getColumnCount();
            String[] row = new String[cols];
            for (int i = 0; i < cols; i++) row[i] = rs.getString(i + 1);
            return row;
        }
    }

    // reads a single string column by id (e.g. account_status for customer_id=1)
    String getStringById(String table, String col, String idCol, int id) throws Exception {
        String sql = "SELECT " + col + " FROM " + table + " WHERE " + idCol + " = ?";
        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            var rs = ps.executeQuery();
            return rs.next() ? rs.getString(1) : null;
        }
    }

    // deletes a row by id (for cleanup after insert tests)
    void deleteRow(String table, String idCol, int id) throws Exception {
        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement("DELETE FROM " + table + " WHERE " + idCol + " = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
