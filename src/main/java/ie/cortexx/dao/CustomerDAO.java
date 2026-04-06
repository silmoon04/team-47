package ie.cortexx.dao;

import ie.cortexx.model.Customer;
import ie.cortexx.enums.AccountStatus;
import ie.cortexx.model.User;
import ie.cortexx.util.DBConnection;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// handles SQL for `customers` table
// important for the whole debt cycle feature
// owner: Fatimah
public class CustomerDAO {

    public Customer findById(int customerId) throws SQLException {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";

        try (var d = DBConnection.getConnection();
             var ps = d.prepareStatement(sql)){
            ps.setInt(1, customerId);
            try (var rs = ps.executeQuery()){
                if (!rs.next()) return null;
                return mapCust(rs);

            }
        }
    }

    public List<Customer> findAll() throws SQLException {
        String sql = "SELECT * FROM customers";
        List<Customer> customers = new ArrayList<>();

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql);
             var rs = ps.executeQuery()){
            while (rs.next()){
                customers.add(mapCust(rs));

            }
        }
        return customers;
    }

    public void save(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (customer_id, account_no, name, " +
            "contact_name, email, phone, address, " +
            "account_status, credit_limit, outstanding_balance, discount_type, " +
            "fixed_discount_rate, flexible_tier_id, data_1st_reminder, " +
            "status_1st_reminder, date_2nd_reminder, status_2nd_reminder, " +
            "created_at, created_by, merchant_id) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (var d = DBConnection.getConnection();

            var ps = d.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, customer.getCustomerId());
            ps.setString(2, customer.getAccountNo());
            ps.setString(3, customer.getName());
            ps.setString(4, customer.getContactName());
            ps.setString(5, customer.getEmail());
            ps.setString(6, customer.getPhone());
            ps.setString(7, customer.getAddress());
            ps.setString(8, customer.getAccountStatus().name());
            ps.setBigDecimal(9, customer.getCreditLimit());
            ps.setBigDecimal(10, customer.getOutstandingBalance());
            ps.setString(11, customer.getDiscountType().name());
            ps.setBigDecimal(12, customer.getFixedDiscountRate());
            ps.setInt(13, customer.getFlexibleTierId());
            ps.setString(14, customer.getDate1stReminder());
            ps.setString(15, customer.getStatus1stReminder().name());
            ps.setString(16, customer.getDate2ndReminder());
            ps.setString(17, customer.getStatus2ndReminder().name());
            ps.setDate(18, customer.getCreatedAt());
            ps.setInt(19, customer.getCreatedBy());
            ps.setInt(20, customer.getMerchantId());


            ps.executeUpdate();

            try (var rs = ps.getGeneratedKeys()){
                if (rs.next()){
                    customer.setCustomerId(rs.getInt(1));
                }
            }
        }
    }

    public void update(Customer customer) throws SQLException {
        String sql = "UPDATE customers SET customer_id = ?, account_no = ?, name = ?," +
            "contact_name = ?, email = ?, phone = ?, address = ?," +
            "account_status = ?, credit_limit = ?, outstanding_balance = ?," +
            "discount_type = ?, fixed_discount_rate = ?, flexible_tier_id = ?, " +
            "data_1st_reminder = ?, status_1st_reminder = ?, date_2nd_reminder = ?, " +
            "status_2nd_reminder = ?, created_at = ?, created_by = ?, merchant_id = ? " +
            "WHERE customer_id = ?";

        try (var d = DBConnection.getConnection();

             var ps = d.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, customer.getCustomerId());
            ps.setString(2, customer.getAccountNo());
            ps.setString(3, customer.getName());
            ps.setString(4, customer.getContactName());
            ps.setString(5, customer.getEmail());
            ps.setString(6, customer.getPhone());
            ps.setString(7, customer.getAddress());
            ps.setString(8, customer.getAccountStatus().name());
            ps.setBigDecimal(9, customer.getCreditLimit());
            ps.setBigDecimal(10, customer.getOutstandingBalance());
            ps.setString(11, customer.getDiscountType().name());
            ps.setBigDecimal(12, customer.getFixedDiscountRate());
            ps.setInt(13, customer.getFlexibleTierId());
            ps.setString(14, customer.getDate1stReminder().toString());
            ps.setString(15, customer.getStatus1stReminder().name());
            ps.setString(16, customer.getDate2ndReminder().toString());
            ps.setString(17, customer.getStatus2ndReminder().name());
            ps.setString(18, customer.getCreatedAt().toString());
            ps.setInt(19, customer.getCreatedBy());
            ps.setInt(20, customer.getMerchantId());

            ps.executeUpdate();
        }

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

    // DONE
    // TODO: update outstanding balance (needed for sales + payments)
    //       same pattern as updateAccountStatus but SET outstanding_balance = ?
    public void updateBalance(int customerId, BigDecimal newBalance) throws SQLException {
        String sql = "UPDATE customers SET outstanding_balance = ? WHERE customer_id = ?";
        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setBigDecimal(1, newBalance);
            ps.setInt(2, customerId);
            ps.executeUpdate();
        }
    }

    // DONE
    // TODO: find customers by status (SELECT WHERE account_status = ?)
    public List<Customer> findByStatus(AccountStatus status) throws SQLException {
        String sql = "SELECT * FROM customers WHERE account_status = ?";

        try (var d = DBConnection.getConnection();
             var ps = d.prepareStatement(sql)){
            ps.setString(1, status.name());
            try (var rs = ps.executeQuery()){
                if (!rs.next()) return null;
                return mapCust(rs);
            }
        }
    }

    // DONE
    // TODO: find all customers with outstanding_balance > 0 (for reminder generation)
    public List<Customer> findDebtors() throws SQLException {
        String sql = "SELECT * FROM customers WHERE outstanding_balance > 0";
        List<Customer> customers = new ArrayList<>();

        try (var c = DBConnection.getConnection();
            var ps = c.prepareStatement(sql)){

            try (var rs = ps.executeQuery()){
                if (!rs.next()) return null;
                customers.add(mapCust(rs));
            }
        }
        return customers;
    }

    // converts a ResultSet row to a Customer object
    private Customer mapCust(ResultSet rs) throws SQLException {
        return Customer.CfromRS(rs);
    }
}

