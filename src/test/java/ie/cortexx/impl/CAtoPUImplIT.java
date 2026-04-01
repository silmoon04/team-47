package ie.cortexx.impl;

import ie.cortexx.exception.ProductNotFoundException;
import ie.cortexx.interfaces.I_CAtoPU;
import ie.cortexx.util.DBConnection;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

// integration tests for I_CAtoPU implementation
// follows HLD pattern: instantiate CAtoPUImpl, cast to I_CAtoPU, call methods
public class CAtoPUImplIT {

    @Test
    void getStockLevel_validProduct() throws Exception {
        String saProductId = getAnyExistingSaProductId();
        assertNotNull(saProductId, "no products found in demo data");

        I_CAtoPU api = new CAtoPUImpl();
        int level = api.getStockLevel(saProductId);

        assertTrue(level >= 0, "stock level should be >= 0");
    }

    @Test
    void getStockLevel_nonexistentProduct() {
        I_CAtoPU api = new CAtoPUImpl();

        assertThrows(ProductNotFoundException.class,
            () -> api.getStockLevel("DOES_NOT_EXIST_999"));
    }

    @Test
    void getStockLevel_nullId() {
        I_CAtoPU api = new CAtoPUImpl();

        assertThrows(IllegalArgumentException.class,
            () -> api.getStockLevel(null));
    }

    @Test
    void deductStock_sufficientStock() throws Exception {
        ProductRow row = getProductWithMinimumStock(2);
        assertNotNull(row, "need a product with stock >= 2 for this test");

        I_CAtoPU api = new CAtoPUImpl();

        int before = api.getStockLevel(row.saProductId);
        boolean ok = api.deductStock(row.saProductId, 1);
        int after = api.getStockLevel(row.saProductId);

        assertTrue(ok, "deductStock should return true when enough stock exists");
        assertEquals(before - 1, after, "stock should decrease by 1");

        restoreStock(row.productId, 1);
    }

    @Test
    void deductStock_insufficientStock() throws Exception {
        ProductRow row = getAnyExistingProductRow();
        assertNotNull(row, "no products found in demo data");

        I_CAtoPU api = new CAtoPUImpl();

        int before = api.getStockLevel(row.saProductId);
        boolean ok = api.deductStock(row.saProductId, before + 1);
        int after = api.getStockLevel(row.saProductId);

        assertFalse(ok, "deductStock should return false when stock is insufficient");
        assertEquals(before, after, "stock should not change when deduction fails");
    }

    @Test
    void deductStock_negativeQty() throws Exception {
        String saProductId = getAnyExistingSaProductId();
        assertNotNull(saProductId, "no products found in demo data");

        I_CAtoPU api = new CAtoPUImpl();

        assertThrows(IllegalArgumentException.class,
            () -> api.deductStock(saProductId, -1));
    }

    // ---- helpers ----

    private String getAnyExistingSaProductId() throws Exception {
        String sql = "SELECT sa_product_id FROM products LIMIT 1";
        try (Connection c = DBConnection.getConnection();
             var rs = c.createStatement().executeQuery(sql)) {
            return rs.next() ? rs.getString(1) : null;
        }
    }

    private ProductRow getAnyExistingProductRow() throws Exception {
        String sql = """
            SELECT p.product_id, p.sa_product_id, s.quantity
            FROM products p
            JOIN stock s ON p.product_id = s.product_id
            LIMIT 1
            """;
        try (Connection c = DBConnection.getConnection();
             var rs = c.createStatement().executeQuery(sql)) {
            if (!rs.next()) return null;
            return new ProductRow(
                rs.getInt("product_id"),
                rs.getString("sa_product_id"),
                rs.getInt("quantity")
            );
        }
    }

    private ProductRow getProductWithMinimumStock(int minQty) throws Exception {
        String sql = """
            SELECT p.product_id, p.sa_product_id, s.quantity
            FROM products p
            JOIN stock s ON p.product_id = s.product_id
            WHERE s.quantity >= ?
            LIMIT 1
            """;
        try (Connection c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setInt(1, minQty);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new ProductRow(
                    rs.getInt("product_id"),
                    rs.getString("sa_product_id"),
                    rs.getInt("quantity")
                );
            }
        }
    }

    private void restoreStock(int productId, int delta) throws Exception {
        String sql = "UPDATE stock SET quantity = quantity + ? WHERE product_id = ?";
        try (Connection c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, productId);
            ps.executeUpdate();
        }
    }

    private record ProductRow(int productId, String saProductId, int quantity) {}
}
