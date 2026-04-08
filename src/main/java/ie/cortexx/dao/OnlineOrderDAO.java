package ie.cortexx.dao;

import ie.cortexx.model.OnlineOrder;
import ie.cortexx.model.OnlineOrderItem;
import ie.cortexx.util.DBConnection;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OnlineOrderDAO {

    public List<OnlineOrder> findAll() throws SQLException {
        String sql = "SELECT * FROM online_orders ORDER BY created_at DESC, online_order_id DESC";
        List<OnlineOrder> orders = new ArrayList<>();

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql);
             var rs = ps.executeQuery()) {
            while (rs.next()) {
                orders.add(mapOrder(rs));
            }

            for (var order : orders) {
                order.setItems(loadItems(c, order.getOnlineOrderId()));
            }
        }

        return orders;
    }

    public void save(OnlineOrder order) throws SQLException {
        String sql = "INSERT INTO online_orders (merchant_id, member_id, order_reference, total_price, discount_applied, status, payment_method, transaction_id, delivery_address) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String itemSql = "INSERT INTO online_order_items (online_order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";

        try (var c = DBConnection.getConnection()) {
            boolean previousAutoCommit = c.getAutoCommit();
            c.setAutoCommit(false);

            try {
                try (var ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, order.getMerchantId() > 0 ? order.getMerchantId() : 1);
                    ps.setString(2, order.getMemberId());
                    ps.setString(3, order.getOrderReference());
                    ps.setBigDecimal(4, order.getTotalPrice());
                    ps.setBigDecimal(5, order.getDiscountApplied() != null ? order.getDiscountApplied() : BigDecimal.ZERO);
                    ps.setString(6, order.getStatus() != null ? order.getStatus() : "CONFIRMED");
                    ps.setString(7, order.getPaymentMethod());
                    ps.setString(8, order.getTransactionId());
                    ps.setString(9, order.getDeliveryAddress());
                    ps.executeUpdate();

                    try (var keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            order.setOnlineOrderId(keys.getInt(1));
                        }
                    }
                }

                if (order.getItems() != null && !order.getItems().isEmpty()) {
                    try (var ps = c.prepareStatement(itemSql)) {
                        for (var item : order.getItems()) {
                            item.setOnlineOrderId(order.getOnlineOrderId());
                            ps.setInt(1, order.getOnlineOrderId());
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

    public void updateStatus(int onlineOrderId, String status) throws SQLException {
        String sql = "UPDATE online_orders SET status = ? WHERE online_order_id = ?";
        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, onlineOrderId);
            ps.executeUpdate();
        }
    }

    private OnlineOrder mapOrder(java.sql.ResultSet rs) throws SQLException {
        OnlineOrder order = new OnlineOrder();
        order.setOnlineOrderId(rs.getInt("online_order_id"));
        order.setMerchantId(rs.getInt("merchant_id"));
        order.setMemberId(rs.getString("member_id"));
        order.setOrderReference(rs.getString("order_reference"));
        order.setDiscountApplied(rs.getBigDecimal("discount_applied"));
        order.setPaymentMethod(rs.getString("payment_method"));
        order.setTransactionId(rs.getString("transaction_id"));
        order.setDeliveryAddress(rs.getString("delivery_address"));
        order.setStatus(rs.getString("status"));
        order.setTotalPrice(rs.getBigDecimal("total_price"));
        var createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            order.setCreatedAt(createdAt.toLocalDateTime());
        }
        var updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            order.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        return order;
    }

    private List<OnlineOrderItem> loadItems(java.sql.Connection c, int onlineOrderId) throws SQLException {
        String sql = "SELECT * FROM online_order_items WHERE online_order_id = ? ORDER BY online_order_item_id";
        List<OnlineOrderItem> items = new ArrayList<>();

        try (var ps = c.prepareStatement(sql)) {
            ps.setInt(1, onlineOrderId);
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    OnlineOrderItem item = new OnlineOrderItem();
                    item.setOnlineOrderItemId(rs.getInt("online_order_item_id"));
                    item.setOnlineOrderId(rs.getInt("online_order_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    items.add(item);
                }
            }
        }

        return items;
    }
}