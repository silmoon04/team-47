package ie.cortexx.dao;

import ie.cortexx.model.Payment;
import java.sql.*;
import java.util.List;

// handles SQL for `payments` table
public class PaymentDAO {

    public void save(Payment payment) throws SQLException {
    }

    public List<Payment> findBySale(int saleId) throws SQLException {
        return null;
    }

    public List<Payment> findByCustomer(int customerId) throws SQLException {
        return null;
    }
}