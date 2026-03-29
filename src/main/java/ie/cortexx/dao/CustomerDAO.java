package ie.cortexx.dao;

import ie.cortexx.model.Customer;
import ie.cortexx.enums.AccountStatus;
import ie.cortexx.util.DBConnection;
import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

// handles SQL for `customers` table
// important for the whole debt cycle feature
// owner: Fatima
public class CustomerDAO {

    public Customer findById(int customerId) throws SQLException {
        return null;
    }

    public List<Customer> findAll() throws SQLException {
        return null;
    }

    public void save(Customer customer) throws SQLException {
    }

    public void update(Customer customer) throws SQLException {
    }

    // -- example method (silmoon) --
    // changes account status (NORMAL -> SUSPENDED -> IN_DEFAULT etc)
    // used by DebtCycleService for auto-suspend/restore/default
    // tested in: DAOExampleIT.updateAccountStatusWorks
    public void updateAccountStatus(int customerId, AccountStatus status) throws SQLException {
        String sql = "UPDATE customers SET account_status = ? WHERE customer_id = ?";
        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, customerId);
            ps.executeUpdate();
        }
    }

    // TODO: update outstanding balance (needed for sales + payments)
    //       same pattern as updateAccountStatus but SET outstanding_balance = ?
    public void updateBalance(int customerId, BigDecimal newBalance) throws SQLException {
    }

    // TODO: find customers by status (SELECT WHERE account_status = ?)
    public List<Customer> findByStatus(AccountStatus status) throws SQLException {
        return null;
    }

    // TODO: find all customers with outstanding_balance > 0 (for reminder generation)
    public List<Customer> findDebtors() throws SQLException {
        return null;
    }
}
