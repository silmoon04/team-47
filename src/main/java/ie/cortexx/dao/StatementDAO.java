package ie.cortexx.dao;

import ie.cortexx.model.Statement;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

// handles SQL for `statements` table
// always scoped to a customer, generated once (never edited)
public class StatementDAO {

    // TODO: SELECT * FROM statements WHERE customer_id = ? ORDER BY period_end DESC
    public List<Statement> findByCustomer(int customerId) throws SQLException {
        return null;
    }

    // TODO: INSERT INTO statements (...) VALUES (...) with RETURN_GENERATED_KEYS
    public void save(Statement statement) throws SQLException {
    }
}
