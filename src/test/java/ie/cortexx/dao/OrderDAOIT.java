package ie.cortexx.dao;

import ie.cortexx.model.Order;
import ie.cortexx.model.OrderItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static ie.cortexx.TestDatabaseHelper.*;
import static org.junit.jupiter.api.Assertions.*;

class OrderDAOIT {

    @Test
    void save_persists_items_and_find_by_id_loads_them() throws Exception {
        OrderDAO dao = new OrderDAO();
        Order order = order();

        dao.save(order);

        try {
            assertTrue(order.getOrderId() > 0);
            Order saved = dao.findById(order.getOrderId());
            assertNotNull(saved);
            assertEquals(1, saved.getItems().size());
        } finally {
            del("orders", "order_id", order.getOrderId());
        }
    }

    @Test
    void find_all_returns_newest_first_for_saved_orders() throws Exception {
        OrderDAO dao = new OrderDAO();
        Order older = order();
        older.setOrderedAt(LocalDateTime.of(2026, 1, 1, 9, 0));
        Order newer = order();
        newer.setOrderedAt(LocalDateTime.of(2026, 1, 2, 9, 0));
        dao.save(older);
        dao.save(newer);

        try {
            int olderIndex = indexOf(dao.findAll(), older.getOrderId());
            int newerIndex = indexOf(dao.findAll(), newer.getOrderId());
            assertTrue(newerIndex < olderIndex);
        } finally {
            del("orders", "order_id", newer.getOrderId());
            del("orders", "order_id", older.getOrderId());
        }
    }

    private Order order() throws Exception {
        int userId = id("SELECT user_id FROM users LIMIT 1");
        int productId = id("SELECT product_id FROM products LIMIT 1");

        Order order = new Order(new BigDecimal("8.00"), userId);
        order.setSaOrderId("IT-" + System.currentTimeMillis());
        order.setOrderedAt(LocalDateTime.now());
        order.getItems().add(new OrderItem(productId, 2, new BigDecimal("4.00")));
        return order;
    }

    private int indexOf(java.util.List<Order> orders, int orderId) {
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getOrderId() == orderId) {
                return i;
            }
        }
        return Integer.MAX_VALUE;
    }
}