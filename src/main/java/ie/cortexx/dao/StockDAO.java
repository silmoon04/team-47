package ie.cortexx.dao;

import ie.cortexx.model.StockItem;
import java.sql.*;
import java.util.List;

// handles SQL for `stock` table (usually joined with products)
public class StockDAO {

    // TODO: get all stock items (join with products for name)
    public List<StockItem> findAll() throws SQLException {
        return null;
    }

    // TODO: find stock for a specific product
    public StockItem findByProductId(int productId) throws SQLException {
        return null;
    }

    // TODO: increase or decrease stock quantity
    //   delta can be positive (restock) or negative (sold)
    public void updateQuantity(int productId, int delta) throws SQLException {
    }

    // TODO: find items where quantity <= reorder_level
    public List<StockItem> findLowStock() throws SQLException {
        return null;
    }
}