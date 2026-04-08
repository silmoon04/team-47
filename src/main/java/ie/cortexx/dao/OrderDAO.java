package ie.cortexx.dao;

import ie.cortexx.model.Order;
import ie.cortexx.enums.OrderStatus;
import ie.cortexx.model.OrderItem;
import ie.cortexx.util.DBConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// handles SQL for `orders` + `order_items` tables
public class OrderDAO {

    public Order findById(int orderId) throws SQLException {
        Order order = null;
        String sql = "SELECT * FROM orders WHERE order_id = ?";

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);

            try (var rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                order = mapOrder(rs);
            }
        }

        String sqlItems = "SELECT * FROM order_items WHERE order_id = ?";
        List<OrderItem> items = new ArrayList<>();

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sqlItems)) {
            ps.setInt(1, orderId);
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setOrderId(rs.getInt("order_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    items.add(item);
                }
            }
        }

        order.setItems(items);
        return order;
    }

    public List<Order> findAll() throws SQLException {
        String sql = "SELECT * FROM orders ORDER BY ordered_at DESC";
        List<Order> orders = new ArrayList<>();

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql);
             var rs = ps.executeQuery()) {
            while (rs.next()) {
                orders.add(mapOrder(rs));
            }
        }
        return orders;
    }

    public void save(Order order) throws SQLException {
        String orderSql = "INSERT INTO orders (sa_order_id, merchant_id, order_status, "
            + "total_amount, ordered_at, delivered_at, ordered_by) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String itemSql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";

        try (var c = DBConnection.getConnection()) {
            boolean previousAutoCommit = c.getAutoCommit();
            c.setAutoCommit(false);

            try {
                try (var ps = c.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                    order.setOrderedAt(order.getOrderedAt() != null ? order.getOrderedAt() : LocalDateTime.now());
                    order.setOrderStatus(order.getOrderStatus() != null ? order.getOrderStatus() : OrderStatus.ACCEPTED);
                    order.setMerchantId(order.getMerchantId() > 0 ? order.getMerchantId() : 1);

                    ps.setString(1, order.getSaOrderId());
                    ps.setInt(2, order.getMerchantId());
                    ps.setString(3, order.getOrderStatus().name());
                    ps.setBigDecimal(4, order.getTotalAmount());
                    ps.setTimestamp(5, Timestamp.valueOf(order.getOrderedAt()));

                    if (order.getDeliveredAt() != null) {
                        ps.setTimestamp(6, Timestamp.valueOf(order.getDeliveredAt()));
                    } else {
                        ps.setNull(6, Types.TIMESTAMP);
                    }

                    ps.setInt(7, order.getOrderedBy());
                    ps.executeUpdate();

                    try (var keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("saving order failed, no id obtained");
                        }
                        order.setOrderId(keys.getInt(1));
                    }
                }

                List<OrderItem> items = order.getItems();
                if (items != null && !items.isEmpty()) {
                    try (var ps = c.prepareStatement(itemSql)) {
                        for (var item : items) {
                            item.setOrderId(order.getOrderId());
                            ps.setInt(1, order.getOrderId());
                            ps.setInt(2, item.getProductId());
                            ps.setInt(3, item.getQuantity());
                            ps.setBigDecimal(4, item.getUnitPrice());
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                }

                c.commit();
            } catch (SQLException error) {
                c.rollback();
                throw error;
            } finally {
                c.setAutoCommit(previousAutoCommit);
            }
        }
    }

    // -- example method --
    // updates order status (ACCEPTED -> PROCESSING -> DISPATCHED -> DELIVERED etc)
    // tested in: DAOExampleIT.updateOrderStatusWorks
    public void updateStatus(int orderId, OrderStatus status) throws SQLException {
        String sql = "UPDATE orders SET order_status = ? WHERE order_id = ?";
        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getInt("order_id"));

        String dbSAOrderId = rs.getString("sa_order_id");
        if (!rs.wasNull()) {
            order.setSaOrderId(dbSAOrderId);
        }

        order.setMerchantId(rs.getInt("merchant_id"));
        order.setOrderStatus(OrderStatus.valueOf(rs.getString("order_status")));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setOrderedAt(rs.getTimestamp("ordered_at").toLocalDateTime());

        Timestamp deliveredAt = rs.getTimestamp("delivered_at");
        if (deliveredAt != null) {
            order.setDeliveredAt(deliveredAt.toLocalDateTime());
        }

        order.setOrderedBy(rs.getInt("ordered_by"));
        return order;
    }
}
