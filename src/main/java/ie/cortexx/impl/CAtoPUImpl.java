package ie.cortexx.impl;

import ie.cortexx.dao.ProductDAO;
import ie.cortexx.dao.StockDAO;
import ie.cortexx.exception.ProductNotFoundException;
import ie.cortexx.exception.ServiceUnavailableException;
import ie.cortexx.interfaces.I_CAtoPU;
import ie.cortexx.model.Product;
import ie.cortexx.model.StockItem;

import java.sql.SQLException;
import java.util.List;

// our implementation of the interface Team C calls
// delegates to ProductDAO + StockDAO for actual db queries
public class CAtoPUImpl implements I_CAtoPU {

    private final StockDAO stockDAO;
    private final ProductDAO productDAO;

    public CAtoPUImpl() {
        this.stockDAO = new StockDAO();
        this.productDAO = new ProductDAO();
    }

    public CAtoPUImpl(StockDAO stockDAO, ProductDAO productDAO) {
        this.stockDAO = stockDAO;
        this.productDAO = productDAO;
    }

    @Override
    public int getStockLevel(String productId)
        throws IllegalArgumentException, ProductNotFoundException {
        validateProductId(productId);

        try {
            Product product = productDAO.findBySaProductId(productId);
            if (product == null) {
                throw new ProductNotFoundException("Product not found: " + productId);
            }

            StockItem stockItem = stockDAO.findByProductId(product.getProductId());
            return stockItem == null ? 0 : stockItem.getQuantity();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get stock level for product: " + productId);
        }
    }

    @Override
    public boolean deductStock(String productId, int quantity)
        throws IllegalArgumentException, ProductNotFoundException {
        validateProductId(productId);

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        try {
            Product product = productDAO.findBySaProductId(productId);
            if (product == null) {
                throw new ProductNotFoundException("Product not found: " + productId);
            }

            StockItem stockItem = stockDAO.findByProductId(product.getProductId());
            int currentQty = stockItem == null ? 0 : stockItem.getQuantity();

            if (currentQty < quantity) {
                return false;
            }

            stockDAO.updateQuantity(product.getProductId(), -quantity);
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to deduct stock for product: " + productId);
        }
    }

    @Override
    public List<StockItem> getAllStock()
        throws ServiceUnavailableException {
        try {
            return stockDAO.findAll();
        } catch (SQLException e) {
            throw new ServiceUnavailableException("Failed to retrieve stock list: " + e.getMessage());
        }
    }

    private void validateProductId(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID must not be null or empty");
        }
    }
}
