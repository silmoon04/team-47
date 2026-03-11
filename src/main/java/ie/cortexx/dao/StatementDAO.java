package ie.cortexx.dao;

import ie.cortexx.model.Statement;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

// handles SQL for `statements` table
public class StatementDAO {

    public void save(Statement statement) throws SQLException {
    }

    public List<Statement> findByCustomerAndPeriod(int customerId, LocalDate start, LocalDate end) throws SQLException {
        return null;
    }
}