package ie.cortexx.dao;

import ie.cortexx.enums.AccountStatus;
import ie.cortexx.enums.OrderStatus;
import ie.cortexx.model.Customer;
import ie.cortexx.model.MerchantDetails;
import ie.cortexx.model.Order;
import ie.cortexx.model.OrderItem;
import ie.cortexx.model.Payment;
import ie.cortexx.model.Sale;
import ie.cortexx.model.SaleItem;
import ie.cortexx.util.DBConnection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// task-gate dao tests
// run only when you want to check a teammate branch:
// mvn verify -DtaskGate.dao=true -Dit.test=TaskGateDAOIT
@EnabledIfSystemProperty(named = "taskGate.dao", matches = "true")
public class TaskGateDAOIT {

    // shakeel: sale save should write header and items
    @Test
    void saleSavePersistsSaleAndItems() throws Exception {
        int userId = id("SELECT user_id FROM users LIMIT 1");
        int productId = id("SELECT product_id FROM products LIMIT 1");
        String productName = str("products", "name", "product_id", productId);

        var sale = new Sale();
        sale.setSoldBy(userId);
        sale.setSubtotal(bd("4.00"));
        sale.setDiscountAmount(bd("0.00"));
        sale.setVatAmount(bd("0.00"));
        sale.setTotalAmount(bd("4.00"));
        sale.setSaleDate(LocalDateTime.now());
        sale.setWalkIn(true);
        sale.setItems(List.of(new SaleItem(productId, productName, 2, bd("2.00"), bd("4.00"))));

        new SaleDAO().save(sale);

        assertTrue(sale.getSaleId() > 0);
        assertEquals(1, count("sale_items", "sale_id", sale.getSaleId()));

        del("sales", "sale_id", sale.getSaleId());
    }

    // shakeel: payment lookup by sale should return linked rows
    @Test
    void paymentFindBySaleReturnsLinkedPayments() throws Exception {
        int saleId = id("SELECT sale_id FROM payments WHERE sale_id IS NOT NULL LIMIT 1");

        var payments = new PaymentDAO().findBySale(saleId);

        assertNotNull(payments);
        assertFalse(payments.isEmpty());
        assertTrue(payments.stream().allMatch(p -> p.getSaleId() == saleId));
    }

    // shakeel: payment lookup by customer should return linked rows
    @Test
    void paymentFindByCustomerReturnsLinkedPayments() throws Exception {
        int customerId = id("SELECT customer_id FROM payments WHERE customer_id IS NOT NULL LIMIT 1");

        var payments = new PaymentDAO().findByCustomer(customerId);

        assertNotNull(payments);
        assertFalse(payments.isEmpty());
        assertTrue(payments.stream().allMatch(p -> p.getCustomerId() == customerId));
    }

    // shakeel: order save should write header and items, then load them back
    @Test
    void orderSaveAndFindByIdRoundTrip() throws Exception {
        int userId = id("SELECT user_id FROM users LIMIT 1");
        int productId = id("SELECT product_id FROM products LIMIT 1");

        var order = new Order();
        order.setSaOrderId("TEST-" + System.currentTimeMillis());
        order.setMerchantId(1);
        order.setOrderStatus(OrderStatus.ACCEPTED);
        order.setTotalAmount(bd("6.00"));
        order.setOrderedAt(LocalDateTime.now());
        order.setOrderedBy(userId);
        order.setItems(List.of(new OrderItem(productId, 3, bd("2.00"))));

        var dao = new OrderDAO();
        dao.save(order);

        assertTrue(order.getOrderId() > 0);

        var loaded = dao.findById(order.getOrderId());
        assertNotNull(loaded);
        assertNotNull(loaded.getItems());
        assertFalse(loaded.getItems().isEmpty());

        del("orders", "order_id", order.getOrderId());
    }

    // shakeel: order history should not be null or empty
    @Test
    void orderFindAllReturnsRows() throws Exception {
        var orders = new OrderDAO().findAll();

        assertNotNull(orders);
        assertFalse(orders.isEmpty());
    }

    // fatima: customer lookups should return seeded data
    @Test
    void customerFindByIdAndFindAllReturnRows() throws Exception {
        var dao = new CustomerDAO();
        int customerId = id("SELECT customer_id FROM customers LIMIT 1");

        Customer customer = dao.findById(customerId);
        List<Customer> all = dao.findAll();

        assertNotNull(customer);
        assertNotNull(customer.getName());
        assertNotNull(all);
        assertFalse(all.isEmpty());
    }

    // fatima: balance update should persist and debtor query should include positive balances
    @Test
    void customerUpdateBalanceAndFindDebtorsWork() throws Exception {
        int customerId = id("SELECT customer_id FROM customers LIMIT 1");
        BigDecimal original = money("customers", "outstanding_balance", "customer_id", customerId);
        BigDecimal updated = original.add(bd("1.00"));

        var dao = new CustomerDAO();
        dao.updateBalance(customerId, updated);

        assertEquals(0, updated.compareTo(money("customers", "outstanding_balance", "customer_id", customerId)));
        assertNotNull(dao.findDebtors());
        assertTrue(dao.findDebtors().stream().anyMatch(c -> c.getCustomerId() == customerId));

        dao.updateBalance(customerId, original);
    }

    // fatima: status query should return rows for a real status
    @Test
    void customerFindByStatusReturnsMatchingRows() throws Exception {
        var customers = new CustomerDAO().findByStatus(AccountStatus.NORMAL);

        assertNotNull(customers);
        assertTrue(customers.stream().allMatch(c -> c.getAccountStatus() == AccountStatus.NORMAL));
    }

    // fatima: merchant details should load and update
    @Test
    void merchantDetailsGetAndUpdateRoundTrip() throws Exception {
        var dao = new MerchantDetailsDAO();
        MerchantDetails md = dao.get();

        assertNotNull(md);
        assertNotNull(md.getBusinessName());

        String originalPhone = md.getPhone();
        md.setPhone("0207 321 8009");
        dao.update(md);

        assertEquals("0207 321 8009", dao.get().getPhone());

        md.setPhone(originalPhone);
        dao.update(md);
    }

    private BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private int id(String sql) throws Exception {
        try (var c = DBConnection.getConnection();
             var rs = c.createStatement().executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private String str(String table, String col, String idCol, int id) throws Exception {
        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement("SELECT " + col + " FROM " + table + " WHERE " + idCol + " = ?")) {
            ps.setInt(1, id);
            var rs = ps.executeQuery();
            return rs.next() ? rs.getString(1) : null;
        }
    }

    private BigDecimal money(String table, String col, String idCol, int id) throws Exception {
        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement("SELECT " + col + " FROM " + table + " WHERE " + idCol + " = ?")) {
            ps.setInt(1, id);
            var rs = ps.executeQuery();
            return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
        }
    }

    private int count(String table, String col, int value) throws Exception {
        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement("SELECT COUNT(*) FROM " + table + " WHERE " + col + " = ?")) {
            ps.setInt(1, value);
            var rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private void del(String table, String idCol, int id) throws Exception {
        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement("DELETE FROM " + table + " WHERE " + idCol + " = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
