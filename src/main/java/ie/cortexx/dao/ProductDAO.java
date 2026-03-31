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

    // insert new product
    public void save(Product product) throws SQLException {
        String sql = "INSERT INTO products (name, sa_product_id, cost_price, markup_rate, is_active) "
            + "VALUES (?, ?, ?, ?, ?)";

        try (var c = DBConnection.getConnection();
            var ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getSaProductId());
            ps.setBigDecimal(3, product.getCostPrice());
            ps.setBigDecimal(4, product.getMarkupRate());
            ps.setBoolean(5, product.isActive());

            ps.executeUpdate();
            try (var rs = ps.getGeneratedKeys()){
                if (rs.next()){
                    product.setProductId(rs.getInt(1));
                }
            }
        }
    }

    // update product
    public void update(Product product) throws SQLException {
        String sql = "UPDATE products SET name = ?, sa_product_id = ?, cost_price = ?, markup_rate = ?, is_active = ? "
            + "WHERE product_id = ?";

        try (var c = DBConnection.getConnection();
            var ps = c.prepareStatement(sql)){
            ps.setString(1, product.getName());
            ps.setString(2, product.getSaProductId());
            ps.setBigDecimal(3, product.getCostPrice());
            ps.setBigDecimal(4, product.getMarkupRate());
            ps.setBoolean(5, product.isActive());
            ps.setInt(6, product.getProductId());

            ps.executeUpdate();
        }
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setName(rs.getString("name"));
        p.setSaProductId(rs.getString("sa_product_id"));
        p.setCostPrice(rs.getBigDecimal("cost_price"));
        p.setMarkupRate(rs.getBigDecimal("markup_rate"));
        p.setActive(rs.getBoolean("is_active"));
        return p;
    }
}
