package ie.cortexx.dao;

import ie.cortexx.model.Product;
import java.sql.*;
import java.util.List;

// handles all SQL for `products` table
public class ProductDAO {

    // TODO: find product by id
    public Product findById(int productId) throws SQLException {
        return null;
    }

    // TODO: get all active products
    public List<Product> findAll() throws SQLException {
        return null;
    }

    // TODO: find by SA catalogue id (for syncing with team A)
    public Product findBySaProductId(String saId) throws SQLException {
        return null;
    }

    // TODO: insert new product
    public void save(Product product) throws SQLException {
    }

    // TODO: update product
    public void update(Product product) throws SQLException {
    }
}