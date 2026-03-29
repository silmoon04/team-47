package ie.cortexx.dao;

import ie.cortexx.model.Payment;
import ie.cortexx.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// handles SQL for `payments` table
// owner: Shakeel
public class PaymentDAO {

    // -- example method --
    // inserts a payment record. card fields are nullable (null for cash payments)
    // tested in: DAOExampleIT.savePaymentCash
    public void save(Payment payment) throws SQLException {
        String sql = "INSERT INTO payments (sale_id, customer_id, payment_type, amount, "
                   + "card_type, card_first4, card_last4, card_expiry, change_given) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // sale_id can be null for standalone account payments
            if (payment.getSaleId() > 0) ps.setInt(1, payment.getSaleId());
            else ps.setNull(1, Types.INTEGER);

            // customer_id can be null for walk-in sales
            if (payment.getCustomerId() > 0) {
                ps.setInt(2, payment.getCustomerId());
            }
            else ps.setNull(2, Types.INTEGER);

            ps.setString(3, payment.getPaymentType().name());
            ps.setBigDecimal(4, payment.getAmount());

            // card fields: null for cash payments
            ps.setString(5, payment.getCardType());
            ps.setString(6, payment.getCardFirst4());
            ps.setString(7, payment.getCardLast4());
            ps.setString(8, payment.getCardExpiry());
            ps.setBigDecimal(9, payment.getChangeGiven());
            ps.executeUpdate();

            try (var keys = ps.getGeneratedKeys()) {
                if (keys.next()) payment.setPaymentId(keys.getInt(1));
            }
        }
    }

    // TODO: find payments by sale_id (SELECT WHERE sale_id = ?)
    public List<Payment> findBySale(int saleId) throws SQLException {
        return null;
    }

    // TODO: find payments by customer_id (SELECT WHERE customer_id = ?)
    public List<Payment> findByCustomer(int customerId) throws SQLException {
        return null;
    }
}
