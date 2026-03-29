package ie.cortexx.dao;

import ie.cortexx.model.Order;
import ie.cortexx.enums.OrderStatus;
import ie.cortexx.util.DBConnection;
import java.sql.*;
import java.util.List;

// handles SQL for `orders` + `order_items` tables
// owner: Shakeel
public class OrderDAO {

    public Order findById(int orderId) throws SQLException {
        return null;
    }

    public List<Order> findAll() throws SQLException {
        return null;
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
}
