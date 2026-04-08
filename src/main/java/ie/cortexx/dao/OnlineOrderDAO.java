package ie.cortexx.dao;

import ie.cortexx.model.OnlineOrder;
import ie.cortexx.util.DBConnection;

import java.sql.SQLException;
import java.sql.Statement;
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
        }

        return orders;
    }

    public void save(OnlineOrder order) throws SQLException {
        String sql = "INSERT INTO online_orders (merchant_id, pu_order_ref, customer_name, customer_email, customer_phone, delivery_address, status, total_amount) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, order.getMerchantId());
            ps.setString(2, order.getPuOrderRef());
            ps.setString(3, order.getCustomerName());
            ps.setString(4, order.getCustomerEmail());
            ps.setString(5, order.getCustomerPhone());
            ps.setString(6, order.getDeliveryAddress());
            ps.setString(7, order.getStatus());
            ps.setBigDecimal(8, order.getTotalAmount());
            ps.executeUpdate();

            try (var keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    order.setOnlineOrderId(keys.getInt(1));
                }
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
        order.setPuOrderRef(rs.getString("pu_order_ref"));
        order.setCustomerName(rs.getString("customer_name"));
        order.setCustomerEmail(rs.getString("customer_email"));
        order.setCustomerPhone(rs.getString("customer_phone"));
        order.setDeliveryAddress(rs.getString("delivery_address"));
        order.setStatus(rs.getString("status"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
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
}