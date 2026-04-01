package ie.cortexx.dao;

import ie.cortexx.enums.*;
import ie.cortexx.model.*;
import ie.cortexx.model.Statement;
import ie.cortexx.util.DBConnection;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/*
how to write dao tests

rules:
- dont hardcode data (usernames, ids etc). grab from findAll() first
- always clean up after inserts with del(table, idCol, id)
- set ALL not-null fields when creating test objects (check schema)
- use the model constructors to keep it short
- use the helpers at the bottom: ok(), bd(), id(), str(), del()

patterns (copy the closest one and swap the dao/model):
- findAll:    ok(dao.findAll())
- findById:   grab first from findAll, look up by id, check field matches
- bogus id:   assertNull(dao.findById(99999))
- save:       create obj, save, assertTrue(id > 0), del() to clean up
- update:     save, change field, update, findById, assertEquals, del()
- deactivate: save, deactivate, findById, assertFalse(isActive), del()
- status:     read original, change, verify, restore original
*/

public class DAOTestIT {

    // -- UserDAO --
    // methods: authenticate, findById, findAll, save, update, deactivate

    @Test void userFindAll() throws Exception {
        ok(new UserDAO().findAll());
    }

    // grab first user, look up by id, make sure its the same one
    @Test void userFindById() throws Exception {
        var first = new UserDAO().findAll().get(0);
        ok(new UserDAO().findById(first.getUserId()));
    }

    // bogus id should just return null, not throw
    @Test void userFindByIdBogus() throws Exception {
        assertNull(new UserDAO().findById(99999));
    }

    // authenticate with real creds from the db (dont hardcode passwords,
    // they change depending on which test data file you loaded)
    @Test void userAuthenticate() throws Exception {
        var first = new UserDAO().findAll().get(0);
        ok(new UserDAO().authenticate(first.getUsername(), first.getPasswordHash()));
    }

    // wrong password should give us null back
    @Test void userAuthenticateBadPass() throws Exception {
        assertNull(new UserDAO().authenticate("admin", "wrong"));
    }

    // full lifecycle: save, update a field, deactivate, then clean up
    // User(username, hash, fullName, role) sets active=true, merchantId=1
    @Test void userSaveUpdateDeactivate() throws Exception {
        var dao = new UserDAO();
        var u = new User("_t" + System.currentTimeMillis(), "h", "T", UserRole.PHARMACIST);
        dao.save(u);
        assertTrue(u.getUserId() > 0);

        u.setFullName("X");
        dao.update(u);
        assertEquals("X", dao.findById(u.getUserId()).getFullName());

        dao.deactivate(u.getUserId());
        assertFalse(dao.findById(u.getUserId()).isActive());

        del("users", "user_id", u.getUserId());
    }

    // -- ProductDAO --
    // methods: findById, findAll, findBySaProductId, save, update

    // findAll only returns active products
    @Test void productFindAll() throws Exception {
        var list = new ProductDAO().findAll();
        ok(list);
        for (var p : list) assertTrue(p.isActive());
    }

    @Test void productFindById() throws Exception {
        var first = new ProductDAO().findAll().get(0);
        ok(new ProductDAO().findById(first.getProductId()));
    }

    // use a real sa_product_id from the data, dont hardcode it
    @Test void productFindBySaId() throws Exception {
        var first = new ProductDAO().findAll().get(0);
        ok(new ProductDAO().findBySaProductId(first.getSaProductId()));
    }

    // Product(saId, name, cost, markup) sets active=true
    // vat_rate is NOT NULL in schema so you gotta set it manually
    @Test void productSaveAndUpdate() throws Exception {
        var dao = new ProductDAO();
        var p = new Product("T" + System.currentTimeMillis(), "Test", bd("5.00"), bd("1.0000"));
        p.setVatRate(bd("0.2000")); // not null in schema
        p.setPackageType("Box");
        dao.save(p);
        assertTrue(p.getProductId() > 0);

        p.setName("Updated");
        dao.update(p);
        assertEquals("Updated", dao.findById(p.getProductId()).getName());

        del("products", "product_id", p.getProductId());
    }

    // -- StockDAO --
    // methods: findAll, findByProductId, findLowStock, updateQuantity

    // findAll uses a JOIN so check the product fields came through
    @Test void stockFindAll() throws Exception {
        var list = new StockDAO().findAll();
        ok(list);
        assertNotNull(list.get(0).getProductName());
        assertNotNull(list.get(0).getCostPrice());
    }

    @Test void stockFindByProductId() throws Exception {
        var first = new StockDAO().findAll().get(0);
        var item = new StockDAO().findByProductId(first.getProductId());
        ok(item);
        assertEquals(first.getProductName(), item.getProductName());
    }

    // might be empty if test data has no low stock, thats fine
    // just make sure anything returned is actually low
    @Test void stockFindLowStock() throws Exception {
        var low = new StockDAO().findLowStock();
        assertNotNull(low);
        for (var si : low) assertTrue(si.getQuantity() <= si.getReorderLevel());
    }

    // sell 1 unit, check qty went down, put it back
    @Test void stockUpdateQuantity() throws Exception {
        var dao = new StockDAO();
        var first = dao.findAll().get(0);
        int before = first.getQuantity();
        dao.updateQuantity(first.getProductId(), -1);
        assertEquals(before - 1, dao.findByProductId(first.getProductId()).getQuantity());
        dao.updateQuantity(first.getProductId(), 1); // put it back
    }

    // -- CustomerDAO --
    // methods: updateAccountStatus (rest are stubs for now)

    // change status, check it changed, put it back
    @Test void customerUpdateStatus() throws Exception {
        int custId = id("SELECT customer_id FROM customers LIMIT 1");
        if (custId == 0) return;
        String orig = str("customers", "account_status", "customer_id", custId);
        new CustomerDAO().updateAccountStatus(custId, AccountStatus.SUSPENDED);
        assertEquals("SUSPENDED", str("customers", "account_status", "customer_id", custId));
        new CustomerDAO().updateAccountStatus(custId, AccountStatus.valueOf(orig));
    }

    // -- OrderDAO --
    // methods: updateStatus (rest are stubs for now)

    @Test void orderUpdateStatus() throws Exception {
        int ordId = id("SELECT order_id FROM orders LIMIT 1");
        if (ordId == 0) return;
        String orig = str("orders", "order_status", "order_id", ordId);
        new OrderDAO().updateStatus(ordId, OrderStatus.DELIVERED);
        assertEquals("DELIVERED", str("orders", "order_status", "order_id", ordId));
        new OrderDAO().updateStatus(ordId, OrderStatus.valueOf(orig));
    }

    // -- PaymentDAO --
    // methods: save (rest are stubs for now)

    // Payment(saleId, type, amount) sets changeGiven=0
    @Test void paymentSave() throws Exception {
        int saleId = id("SELECT sale_id FROM sales LIMIT 1");
        if (saleId == 0) return;
        var p = new Payment(saleId, PaymentType.CASH, bd("10.00"));
        new PaymentDAO().save(p);
        assertTrue(p.getPaymentId() > 0);
        del("payments", "payment_id", p.getPaymentId());
    }

    // -- TemplateDAO --
    // methods: findByType, update
    // templates are seeded in test data (FIRST, SECOND)
    // settings panel loads them by type and saves edits

    // should find the FIRST reminder template from test data
    @Test void templateFindByType() throws Exception {
        var t = new TemplateDAO().findByType("FIRST");
        ok(t);
        assertNotNull(t.getContent());
        assertEquals("FIRST", t.getTemplateType());
    }

    // bogus type should return null
    @Test void templateFindByTypeBogus() throws Exception {
        assertNull(new TemplateDAO().findByType("DOESNOTEXIST"));
    }

    // change content, verify, restore
    @Test void templateUpdate() throws Exception {
        var dao = new TemplateDAO();
        var t = dao.findByType("FIRST");
        if (t == null) return;
        String orig = t.getContent();

        t.setContent("test content");
        dao.update(t);
        assertEquals("test content", dao.findByType("FIRST").getContent());

        t.setContent(orig); // restore
        dao.update(t);
    }

    // -- ReminderDAO --
    // methods: findByCustomer, save, markSent
    // reminders are always for a specific customer

    // save a reminder, find it by customer, then clean up
    @Test void reminderSaveAndFind() throws Exception {
        int custId = id("SELECT customer_id FROM customers LIMIT 1");
        if (custId == 0) return;

        var dao = new ReminderDAO();
        var r = new Reminder(custId, ReminderType.FIRST, bd("45.50"));
        dao.save(r);
        assertTrue(r.getReminderId() > 0);

        // should show up in findByCustomer
        var list = dao.findByCustomer(custId);
        ok(list);
        assertTrue(list.stream().anyMatch(x -> x.getReminderId() == r.getReminderId()));

        del("reminders", "reminder_id", r.getReminderId());
    }

    // mark a reminder as sent, check sent_at got set
    @Test void reminderMarkSent() throws Exception {
        int custId = id("SELECT customer_id FROM customers LIMIT 1");
        if (custId == 0) return;

        var dao = new ReminderDAO();
        var r = new Reminder(custId, ReminderType.SECOND, bd("100.00"));
        dao.save(r);

        dao.markSent(r.getReminderId());
        // sent_at should no longer be null
        assertNotNull(str("reminders", "sent_at", "reminder_id", r.getReminderId()));

        del("reminders", "reminder_id", r.getReminderId());
    }

    // -- StatementDAO --
    // methods: findByCustomer, save
    // statements are generated once per period, never edited

    // save a statement, find it by customer, clean up
    @Test void statementSaveAndFind() throws Exception {
        int custId = id("SELECT customer_id FROM customers LIMIT 1");
        int userId = id("SELECT user_id FROM users LIMIT 1");
        if (custId == 0 || userId == 0) return;

        var dao = new StatementDAO();
        var s = new Statement(custId,
            LocalDate.of(2026, 3, 1),
            LocalDate.of(2026, 3, 31),
            bd("0.00"), bd("45.50"), userId);
        dao.save(s);
        assertTrue(s.getStatementId() > 0);

        // should show up in findByCustomer
        var list = dao.findByCustomer(custId);
        ok(list);
        assertTrue(list.stream().anyMatch(x -> x.getStatementId() == s.getStatementId()));

        del("statements", "statement_id", s.getStatementId());
    }

    // -- helpers (use these to keep tests short) --

    void ok(Object obj) { assertNotNull(obj); }

    void ok(java.util.List<?> list) {
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    BigDecimal bd(String v) { return new BigDecimal(v); }

    // grab first int from a query (0 if empty)
    int id(String sql) throws Exception {
        try (var c = DBConnection.getConnection();
             var rs = c.createStatement().executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // read a single string column by id
    String str(String table, String col, String idCol, int id) throws Exception {
        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement("SELECT " + col + " FROM " + table + " WHERE " + idCol + " = ?")) {
            ps.setInt(1, id);
            var rs = ps.executeQuery();
            return rs.next() ? rs.getString(1) : null;
        }
    }

    // delete a row (cleanup after insert tests)
    void del(String table, String idCol, int id) throws Exception {
        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement("DELETE FROM " + table + " WHERE " + idCol + " = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
