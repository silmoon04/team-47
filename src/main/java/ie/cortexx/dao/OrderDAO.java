package ie.cortexx.dao;

import ie.cortexx.model.Order;
import ie.cortexx.enums.OrderStatus;
import ie.cortexx.model.OrderItem;
import ie.cortexx.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// handles SQL for `orders` + `order_items` tables
// owner: Shakeel
// TODO: extend this DAO for PU direct-DB online orders once the online_orders schema lands.
public class OrderDAO {

    // TODO: load one SA order with its item rows for order-history drilldown and invoice viewing.
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

    // TODO: return all persisted SA orders for the Orders screen instead of placeholder rows.
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

    // TODO: save order + items in a transaction (see JDBC_AND_DAO_GUIDE.md transaction section)
    public void save(Order order) throws SQLException {
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
