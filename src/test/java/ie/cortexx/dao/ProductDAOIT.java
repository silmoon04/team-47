package ie.cortexx.dao;

import ie.cortexx.model.Product;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ProductDAOIT {

    @Test
    void findAllReturnsActiveProducts() throws Exception {
        ProductDAO dao = new ProductDAO();
        List<Product> products = dao.findAll();

        assertNotNull(products);
        assertFalse(products.isEmpty(), "expected active products from database");

        for (Product p : products) {
            assertTrue(p.isActive(), "findAll should only return active products");
            assertNotNull(p.getSaProductId());
            assertNotNull(p.getName());
        }
    }
}
