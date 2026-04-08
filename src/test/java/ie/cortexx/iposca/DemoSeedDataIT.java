package ie.cortexx.iposca;

import ie.cortexx.util.DBConnection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DemoSeedDataIT {

    @Test
    void demoUsersCoverCoreRoles() throws Exception {
        assertTrue(count("SELECT COUNT(DISTINCT role) FROM users WHERE role IN ('ADMIN','MANAGER','PHARMACIST')") >= 3);
    }

    @Test
    void puOrderStatusesCoverMainWorkflow() throws Exception {
        for (String status : new String[]{"CONFIRMED", "RECEIVED", "PROCESSING", "READY", "DISPATCHED", "DELIVERED", "CANCELLED"}) {
            assertTrue(count("SELECT COUNT(*) FROM online_orders WHERE status = '" + status + "'") >= 1, "missing status: " + status);
        }
    }

    @Test
    void puOrdersIncludeDeliveryDetailsAndLineItems() throws Exception {
        assertTrue(count("SELECT COUNT(*) FROM online_orders WHERE delivery_address IS NOT NULL AND TRIM(delivery_address) <> ''") >= 6);
        assertTrue(count("SELECT COUNT(*) FROM online_order_items") >= 7);
    }

    @Test
    void reminderTemplatesNeededForDemoAreSeeded() throws Exception {
        assertEquals(1, count("SELECT COUNT(*) FROM templates WHERE template_type = 'FIRST'"));
        assertEquals(1, count("SELECT COUNT(*) FROM templates WHERE template_type = 'SECOND'"));
    }

    @Test
    void debtAndLowStockDemoEdgesExist() throws Exception {
        assertTrue(count("SELECT COUNT(*) FROM customers WHERE outstanding_balance > 0") >= 3);
        assertTrue(count("SELECT COUNT(*) FROM stock WHERE quantity <= reorder_level") >= 3);
    }

    private int count(String sql) throws Exception {
        try (var c = DBConnection.getConnection();
             var rs = c.createStatement().executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }
}