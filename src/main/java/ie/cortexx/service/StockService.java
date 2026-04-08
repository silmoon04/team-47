package ie.cortexx.service;

import ie.cortexx.dao.StockDAO;
import ie.cortexx.model.StockItem;

import java.sql.SQLException;
import java.util.List;

// stock queries, low stock alerts, manual adjustments
public class StockService {
    private final StockDAO stockDAO;

    public StockService() {
        this(new StockDAO());
    }

    public StockService(StockDAO stockDAO) {
        this.stockDAO = stockDAO;
    }

    public List<StockItem> findAll() throws SQLException {
        return stockDAO.findAll();
    }

    public List<StockItem> findLowStock() throws SQLException {
        return stockDAO.findLowStock();
    }

    public void addStock(int productId, int quantity) throws SQLException {
        stockDAO.updateQuantity(productId, quantity);
    }
}