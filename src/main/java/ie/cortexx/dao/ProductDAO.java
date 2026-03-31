package ie.cortexx.dao;

import ie.cortexx.model.Product;
import ie.cortexx.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// handles all SQL for `products` table
public class ProductDAO {

    // find product by id
    public Product findById(int productId) throws SQLException {
        String sql = "SELECT * FROM products WHERE product_id = ?";

        try (var c = DBConnection.getConnection();
            var ps = c.prepareStatement(sql)){
            ps.setInt(1, productId);

            try (var rs = ps.executeQuery()){
                if (!rs.next()) return null;
                return mapProduct(rs);
            }
        }
    }

    // get all active products
    public List<Product> findAll() throws SQLException {
        String sql = "SELECT * FROM products WHERE is_active = TRUE";
        List<Product> products = new ArrayList<>();

        try (var c = DBConnection.getConnection();
            var ps = c.prepareStatement(sql);
            var rs = ps.executeQuery()){
            while (rs.next()){
                products.add(mapProduct(rs));
            }
        }
        return products;
    }

    // find by SA catalogue id (for syncing with team A)
    public Product findBySaProductId(String saId) throws SQLException {
        String sql = "SELECT * FROM products WHERE sa_product_id = ?";

        try (var c = DBConnection.getConnection();
            var ps = c.prepareStatement(sql)){
            ps.setString(1, saId);

            try (var rs = ps.executeQuery()){
                if (!rs.next()) return null;
                return mapProduct(rs);
            }
        }
    }

    // TODO: insert new product
    public void save(Product product) throws SQLException {
    }

    // TODO: update product
    public void update(Product product) throws SQLException {
    }
}
