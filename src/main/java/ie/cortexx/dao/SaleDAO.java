package ie.cortexx.dao;

import ie.cortexx.model.Sale;
import ie.cortexx.model.SaleItem;
import ie.cortexx.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// handles SQL for `sales` + `sale_items` tables
// save() needs to use a transaction to insert into both tables
public class SaleDAO {

    public void save(Sale sale) throws SQLException {
        DBConnection.withTransaction(connection -> {
            save(connection, sale);
            return null;
        });
    }

    public void save(Connection c, Sale sale) throws SQLException {
        String saleSql = "INSERT INTO sales (customer_id, sold_by, subtotal, discount_amount, "
            + "vat_amount, total_amount, sale_date, payment_method, is_walk_in) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String itemSql = "INSERT INTO sale_items (sale_id, product_id, product_name, quantity, "
            + "unit_price, discount_rate, line_total) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (var ps = c.prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS)) {
            sale.setSaleDate(sale.getSaleDate() != null ? sale.getSaleDate() : LocalDateTime.now());

            if (sale.getCustomerId() != null) {
                ps.setInt(1, sale.getCustomerId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }

            ps.setInt(2, sale.getSoldBy());
            ps.setBigDecimal(3, sale.getSubtotal());
            ps.setBigDecimal(4, sale.getDiscountAmount() != null ? sale.getDiscountAmount() : BigDecimal.ZERO);
            ps.setBigDecimal(5, sale.getVatAmount() != null ? sale.getVatAmount() : BigDecimal.ZERO);
            ps.setBigDecimal(6, sale.getTotalAmount());
            ps.setTimestamp(7, Timestamp.valueOf(sale.getSaleDate()));

            if (sale.getPaymentMethod() != null) {
                ps.setString(8, sale.getPaymentMethod());
            } else {
                ps.setNull(8, Types.VARCHAR);
            }

            ps.setBoolean(9, sale.isWalkIn());
            ps.executeUpdate();

            try (var keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("saving sale failed, no id obtained");
                }
                sale.setSaleId(keys.getInt(1));
            }
        }

        List<SaleItem> items = sale.getItems();
        if (items != null && !items.isEmpty()) {
            try (var ps = c.prepareStatement(itemSql)) {
                for (var item : items) {
                    item.setSaleId(sale.getSaleId());
                    ps.setInt(1, sale.getSaleId());
                    ps.setInt(2, item.getProductId());
                    ps.setString(3, item.getProductName());
                    ps.setInt(4, item.getQuantity());
                    ps.setBigDecimal(5, item.getUnitPrice());
                    ps.setBigDecimal(6, item.getDiscountRate() != null ? item.getDiscountRate() : BigDecimal.ZERO);
                    ps.setBigDecimal(7, item.getLineTotal());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }
    }

    public List<Sale> findByDateRange(LocalDate from, LocalDate to) throws SQLException {
        String sql = "SELECT * FROM sales WHERE sale_date BETWEEN ? AND ?";
        List<Sale> sales = new ArrayList<>();

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));

            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    sales.add(mapSale(rs));
                }
            }
        }

        return sales;
    }

    public List<Sale> findByCustomer(int customerId) throws SQLException {
        String sql = "SELECT * FROM sales WHERE customer_id = ?";
        List<Sale> sales = new ArrayList<>();

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setInt(1, customerId);

            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    sales.add(mapSale(rs));
                }
            }
        }

        return sales;
    }

    public int countItems(int saleId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM sale_items WHERE sale_id = ?";

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setInt(1, saleId);
            try (var rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private Sale mapSale(ResultSet rs) throws SQLException {
        Sale sale = new Sale();
        sale.setSaleId(rs.getInt("sale_id"));

        int dbCustomerId = rs.getInt("customer_id");
        sale.setCustomerId(rs.wasNull() ? null : dbCustomerId);

        sale.setSoldBy(rs.getInt("sold_by"));
        sale.setSubtotal(rs.getBigDecimal("subtotal"));
        sale.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        sale.setVatAmount(rs.getBigDecimal("vat_amount"));
        sale.setTotalAmount(rs.getBigDecimal("total_amount"));

        Timestamp saleDate = rs.getTimestamp("sale_date");
        sale.setSaleDate(saleDate != null ? saleDate.toLocalDateTime() : null);

        sale.setPaymentMethod(rs.getString("payment_method"));
        sale.setWalkIn(rs.getBoolean("is_walk_in"));
        return sale;
    }
}