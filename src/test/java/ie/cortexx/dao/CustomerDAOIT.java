package ie.cortexx.dao;

import ie.cortexx.enums.DiscountType;
import ie.cortexx.enums.ReminderType;
import ie.cortexx.model.Customer;
import ie.cortexx.model.Reminder;
import ie.cortexx.util.DBConnection;
import org.junit.jupiter.api.Test;

import java.sql.Types;

import static ie.cortexx.TestDatabaseHelper.bd;
import static ie.cortexx.TestDatabaseHelper.count;
import static org.junit.jupiter.api.Assertions.*;

class CustomerDAOIT {

    @Test
    void findByIdReturnsSeededCustomer() throws Exception {
        int customerId = ie.cortexx.TestDatabaseHelper.id("SELECT customer_id FROM customers LIMIT 1");

        Customer customer = new CustomerDAO().findById(customerId);

        assertNotNull(customer);
        assertEquals(customerId, customer.getCustomerId());
        assertNotNull(customer.getName());
    }

    @Test
    void saveAndUpdateBalanceRoundTrip() throws Exception {
        var customer = new Customer("task customer", "1 test street", DiscountType.FIXED);
        customer.setAccountNo("CUST-" + System.currentTimeMillis());
        customer.setContactName("task contact");
        customer.setFixedDiscountRate(bd("0.0300"));

        var dao = new CustomerDAO();
        dao.save(customer);

        try {
            assertTrue(customer.getCustomerId() > 0);

            dao.updateBalance(customer.getCustomerId(), bd("25.00"));
            Customer reloaded = dao.findById(customer.getCustomerId());

            assertNotNull(reloaded);
            assertEquals(bd("25.00"), reloaded.getOutstandingBalance());
        } finally {
            if (dao.findById(customer.getCustomerId()) != null) {
                dao.delete(customer.getCustomerId());
            }
        }
    }

    @Test
    void deleteRemovesCustomerAndLinkedRows() throws Exception {
        var customer = new Customer("cascade customer", "7 test street", DiscountType.FIXED);
        customer.setAccountNo("CASC-" + System.currentTimeMillis());
        customer.setContactName("Cascade Contact");
        customer.setFixedDiscountRate(bd("0.0200"));

        var customerDAO = new CustomerDAO();
        customerDAO.save(customer);

        try {
            insertDiscountTier(customer.getCustomerId());

            var reminder = new Reminder(customer.getCustomerId(), ReminderType.FIRST, bd("12.00"));
            reminder.setContent("cascade reminder");
            new ReminderDAO().save(reminder);

            assertEquals(1, count("discount_tiers", "customer_id", customer.getCustomerId()));
            assertEquals(1, count("reminders", "customer_id", customer.getCustomerId()));

            customerDAO.delete(customer.getCustomerId());

            assertNull(customerDAO.findById(customer.getCustomerId()));
            assertEquals(0, count("discount_tiers", "customer_id", customer.getCustomerId()));
            assertEquals(0, count("reminders", "customer_id", customer.getCustomerId()));
        } finally {
            if (customerDAO.findById(customer.getCustomerId()) != null) {
                customerDAO.delete(customer.getCustomerId());
            }
        }
    }

    private void insertDiscountTier(int customerId) throws Exception {
        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement("INSERT INTO discount_tiers (customer_id, tier_name, min_monthly_spend, discount_rate) VALUES (?, ?, ?, ?)")) {
            ps.setInt(1, customerId);
            ps.setString(2, "Cascade Tier");
            ps.setBigDecimal(3, bd("100.00"));
            ps.setBigDecimal(4, bd("0.0200"));
            ps.executeUpdate();
        }
    }
}