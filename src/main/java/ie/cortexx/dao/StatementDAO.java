package ie.cortexx.dao;

import ie.cortexx.model.Statement;
import ie.cortexx.util.DBConnection;


import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

// handles SQL for `statements` table
// always scoped to a customer, generated once (never edited)
public class StatementDAO {

    // SELECT * FROM statements WHERE customer_id = ? ORDER BY period_end DESC
    public List<Statement> findByCustomer(int customerId) throws SQLException {
        String sql = "SELECT * FROM statements WHERE customer_id = ? ORDER BY period_end DESC";
        List<Statement> statements = new ArrayList<>();

        try (var c = DBConnection.getConnection();
            var ps = c.prepareStatement(sql)){
            ps.setInt(1, customerId);

            try (var rs = ps.executeQuery()){
                while (rs.next()){
                    statements.add(mapStatement(rs));
                }
            }
        }
        return statements;
    }
    private Statement mapStatement(ResultSet rs) throws SQLException {
        Statement s = new Statement();
        s.setStatementId(rs.getInt("statement_id"));
        s.setCustomerId(rs.getInt("customer_id"));
        s.setPeriodStart(rs.getDate("period_start").toLocalDate());
        s.setPeriodEnd(rs.getDate("period_end").toLocalDate());
        s.setOpeningBalance(rs.getBigDecimal("opening_balance"));
        s.setTotalPurchases(rs.getBigDecimal("total_purchases"));
        s.setTotalPayments(rs.getBigDecimal("total_payments"));
        s.setClosingBalance(rs.getBigDecimal("closing_balance"));
        s.setGeneratedBy(rs.getInt("generated_by"));
        return s;
    }

    // INSERT INTO statements (...) VALUES (...) with RETURN_GENERATED_KEYS
    public void save(Statement statement) throws SQLException {
        String sql = "INSERT INTO statements "
            + "(customer_id, period_start, period_end, opening_balance, total_purchases, total_payments, closing_balance, generated_by) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, statement.getCustomerId());
            ps.setDate(2, Date.valueOf(statement.getPeriodStart()));
            ps.setDate(3, Date.valueOf(statement.getPeriodEnd()));
            ps.setBigDecimal(4, statement.getOpeningBalance());
            ps.setBigDecimal(5, statement.getTotalPurchases());
            ps.setBigDecimal(6, statement.getTotalPayments());
            ps.setBigDecimal(7, statement.getClosingBalance());
            ps.setInt(8, statement.getGeneratedBy());

            ps.executeUpdate();

            try (var rs=ps.getGeneratedKeys()) {
                if (rs.next()){
                    statement.setStatementId(rs.getInt(1));
                }
            }

        }
    }
}
