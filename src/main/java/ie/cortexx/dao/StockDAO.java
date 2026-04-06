package ie.cortexx.dao;

import ie.cortexx.model.StockItem;
import ie.cortexx.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// handles SQL for `stock` table (usually joined with products for display)
// owner: Alex
public class StockDAO {

    //get all stock items (same pattern as findLowStock but without WHERE)
    public List<StockItem> findAll() throws SQLException {
        String sql = "SELECT s.stock_id, s.product_id, s.quantity, s.reorder_level, "
            + "p.name, p.sa_product_id, p.cost_price, p.markup_rate "
            + "FROM stock s JOIN products p ON s.product_id = p.product_id "
            + "WHERE p.is_active = TRUE";

        List<StockItem> items = new ArrayList<>();
        try (var c = DBConnection.getConnection();
            var rs = c.createStatement().executeQuery(sql)){
            while (rs.next()){
                items.add(mapStockItem(rs));
            }
        }
        return items;
    }

    // find stock for a specific product (add WHERE s.product_id = ?)
    public StockItem findByProductId(int productId) throws SQLException {
        String sql = "SELECT s.stock_id, s.product_id, s.quantity, s.reorder_level, "
            + "p.name, p.sa_product_id, p.cost_price, p.markup_rate "
            + "FROM stock s JOIN products p ON s.product_id = p.product_id "
            + "WHERE s.product_id = ?";

        try (var c = DBConnection.getConnection();
            var ps = c.prepareStatement(sql)){
            ps.setInt(1, productId);

            try (var rs = ps.executeQuery()){
                if (!rs.next()) return null;
                return mapStockItem(rs);
            }
        }
    }

    // -- example method (silmoon) --
    // delta can be +10 (delivery restocked) or -3 (sold 3 units)
    // the CHECK (quantity >= 0) in the schema will reject if it goes negative
    // tested in: DAOExampleIT.updateQuantityDeductsStock
    public void updateQuantity(int productId, int delta) throws SQLException {
        String sql = "UPDATE stock SET quantity = quantity + ? WHERE product_id = ?";
        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, productId);
            ps.executeUpdate();
        }
    }

    // -- example method (silmoon) --
    // finds items where current qty is at or below reorder level
    // shows the JOIN pattern needed for findAll/findByProductId too
    // tested in: DAOExampleIT.findLowStockReturnsCorrectItems
    public List<StockItem> findLowStock() throws SQLException {
        String sql = "SELECT s.stock_id, s.product_id, s.quantity, s.reorder_level, "
                   + "p.name, p.sa_product_id, p.cost_price, p.markup_rate "
                   + "FROM stock s JOIN products p ON s.product_id = p.product_id "
                   + "WHERE s.quantity <= s.reorder_level";
        List<StockItem> items = new ArrayList<>();
        try (var c = DBConnection.getConnection();
             var rs = c.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                items.add(mapStockItem(rs));
            }
        }
        return items;
    }

    // converts a ResultSet row (from the JOIN query) to a StockItem
    private StockItem mapStockItem(ResultSet rs) throws SQLException {
        StockItem si = new StockItem();
        si.setStockId(rs.getInt("stock_id"));
        si.setProductId(rs.getInt("product_id"));
        si.setQuantity(rs.getInt("quantity"));
        si.setReorderLevel(rs.getInt("reorder_level"));
        si.setProductName(rs.getString("name"));
        si.setSaProductId(rs.getString("sa_product_id"));
        si.setCostPrice(rs.getBigDecimal("cost_price"));
        si.setMarkupRate(rs.getBigDecimal("markup_rate"));
        return si;
    }
}
