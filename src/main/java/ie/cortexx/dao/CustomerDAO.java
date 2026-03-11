package ie.cortexx.dao;

import ie.cortexx.model.Customer;
import ie.cortexx.enums.AccountStatus;
import java.sql.*;
import java.util.List;

// handles SQL for `customers` table
// important for the whole debt cycle feature
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

    // TODO: change account status (normal -> suspended -> default etc)
    public void updateAccountStatus(int customerId, AccountStatus status) throws SQLException {
    }
}