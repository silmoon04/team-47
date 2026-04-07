package ie.cortexx.dao;

import ie.cortexx.enums.AccountStatus;
import ie.cortexx.model.Customer;
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

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return mapCust(rs);
            }
        }
    }

    public List<Customer> findAll() throws SQLException {
        String sql = "SELECT * FROM customers";

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql);
             var rs = ps.executeQuery()) {
            return mapCustomerList(rs);
        }
    }

    public void save(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (" +
            "account_no, name, contact_name, email, phone, address, " +
            "account_status, credit_limit, outstanding_balance, discount_type, " +
            "fixed_discount_rate, flexible_tier_id, " +
            "debt_period_start, last_payment_date, " +
            "date_1st_reminder, status_1st_reminder, " +
            "date_2nd_reminder, status_2nd_reminder, " +
            "created_at, created_by, merchant_id" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            bindCustomerFields(ps, customer);
            ps.executeUpdate();

            try (var rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    customer.setCustomerId(rs.getInt(1));
                }
            }
        }
    }

    public void update(Customer customer) throws SQLException {
        String sql = "UPDATE customers SET " +
            "account_no = ?, name = ?, contact_name = ?, email = ?, phone = ?, address = ?, " +
            "account_status = ?, credit_limit = ?, outstanding_balance = ?, discount_type = ?, " +
            "fixed_discount_rate = ?, flexible_tier_id = ?, " +
            "debt_period_start = ?, last_payment_date = ?, " +
            "date_1st_reminder = ?, status_1st_reminder = ?, " +
            "date_2nd_reminder = ?, status_2nd_reminder = ?, " +
            "created_at = ?, created_by = ?, merchant_id = ? " +
            "WHERE customer_id = ?";

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {

            bindCustomerFields(ps, customer);
            ps.setInt(22, customer.getCustomerId());

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

    // updates outstanding balance (needed for sales + payments)
    public void updateBalance(int customerId, BigDecimal newBalance) throws SQLException {
        String sql = "UPDATE customers SET outstanding_balance = ? WHERE customer_id = ?";
        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setBigDecimal(1, newBalance);
            ps.setInt(2, customerId);
            ps.executeUpdate();
        }
    }

    // find customers by status
    public List<Customer> findByStatus(AccountStatus status) throws SQLException {
        String sql = "SELECT * FROM customers WHERE account_status = ?";

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {

            ps.setString(1, status.name());
            try (var rs = ps.executeQuery()) {
                return mapCustomerList(rs);
            }
        }
    }

    // find all customers with outstanding_balance > 0
    public List<Customer> findDebtors() throws SQLException {
        String sql = "SELECT * FROM customers WHERE outstanding_balance > 0";
        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql);
             var rs = ps.executeQuery()) {

            return mapCustomerList(rs);
        }
    }

    // helper for the common customer fields for INSERT / UPDATE
    private void bindCustomerFields(PreparedStatement ps, Customer customer) throws SQLException {
        ps.setString(1, customer.getAccountNo());
        ps.setString(2, customer.getName());
        ps.setString(3, customer.getContactName());
        ps.setString(4, customer.getEmail());
        ps.setString(5, customer.getPhone());
        ps.setString(6, customer.getAddress());
        ps.setString(7, customer.getAccountStatus() != null ? customer.getAccountStatus().name() : null);
        ps.setBigDecimal(8, customer.getCreditLimit());
        ps.setBigDecimal(9, customer.getOutstandingBalance());
        ps.setString(10, customer.getDiscountType() != null ? customer.getDiscountType().name() : null);
        ps.setBigDecimal(11, customer.getFixedDiscountRate());
        ps.setObject(12, customer.getFlexibleTierId(), Types.INTEGER);
        ps.setDate(13, customer.getDebtPeriodStart() != null ? Date.valueOf(customer.getDebtPeriodStart()) : null);
        ps.setDate(14, customer.getLastPaymentDate() != null ? Date.valueOf(customer.getLastPaymentDate()) : null);
        ps.setDate(15, customer.getDate1stReminder() != null ? Date.valueOf(customer.getDate1stReminder()) : null);
        ps.setString(16, customer.getStatus1stReminder() != null ? customer.getStatus1stReminder().name() : null);
        ps.setDate(17, customer.getDate2ndReminder() != null ? Date.valueOf(customer.getDate2ndReminder()) : null);
        ps.setString(18, customer.getStatus2ndReminder() != null ? customer.getStatus2ndReminder().name() : null);
        ps.setTimestamp(19, customer.getCreatedAt() != null ? Timestamp.valueOf(customer.getCreatedAt()) : null);
        ps.setObject(20, customer.getCreatedBy(), Types.INTEGER);
        ps.setInt(21, customer.getMerchantId());
    }

    // converts a ResultSet row to a Customer object
    private Customer mapCust(ResultSet rs) throws SQLException {
        return Customer.CfromRS(rs);
    }

    // converts all rows in a ResultSet to a list of Customer objects
    private List<Customer> mapCustomerList(ResultSet rs) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        while (rs.next()) {
            customers.add(mapCust(rs));
        }
        return customers;
    }
}
