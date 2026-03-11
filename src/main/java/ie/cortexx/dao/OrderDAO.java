package ie.cortexx.dao;

import ie.cortexx.model.Order;
import ie.cortexx.enums.OrderStatus;
import java.sql.*;
import java.util.List;

// handles SQL for `orders` + `order_items` tables
public class OrderDAO {

    public Order findById(int orderId) throws SQLException {
        return null;
    }

    public List<Order> findAll() throws SQLException {
        return null;
    }

    // TODO: save order + items in a transaction
    public void save(Order order) throws SQLException {
    }

    public void updateStatus(int orderId, OrderStatus status) throws SQLException {
    }
}