package ie.cortexx.dao;

import ie.cortexx.model.Sale;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

// handles SQL for `sales` + `sale_items` tables
// save() needs to use a transaction to insert into both tables
public class SaleDAO {

    // TODO: insert sale + all sale items in one transaction
    public void save(Sale sale) throws SQLException {
    }

    // TODO: get sales within a date range (for reports)
    public List<Sale> findByDateRange(LocalDate from, LocalDate to) throws SQLException {
        return null;
    }

    // TODO: get sales for a specific customer
    public List<Sale> findByCustomer(int customerId) throws SQLException {
        return null;
    }
}