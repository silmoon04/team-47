package ie.cortexx.dao;

import ie.cortexx.TestDatabaseHelper;
import ie.cortexx.model.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ProductDAOIT {

    @BeforeEach
    void setUp() throws Exception {
        TestDatabaseHelper.useTestDatabase();
        TestDatabaseHelper.seedTestData();
    }

    @AfterEach
    void tearDown() throws Exception {
        TestDatabaseHelper.cleanTestData();
        TestDatabaseHelper.useMainDatabase();
    }

    @Test
    void findAllReturnsOnlyActiveSeededProducts() throws Exception {
        var dao = new ProductDAO();
        var products = dao.findAll();

        assertNotNull(products);
        assertTrue(products.size() >= 3);

        assertTrue(products.stream().allMatch(Product::isActive));
        assertTrue(products.stream().anyMatch(p -> TestDatabaseHelper.PRODUCT_OK.equals(p.getSaProductId())));
        assertTrue(products.stream().anyMatch(p -> TestDatabaseHelper.PRODUCT_ZERO.equals(p.getSaProductId())));
        assertTrue(products.stream().anyMatch(p -> TestDatabaseHelper.PRODUCT_LOW.equals(p.getSaProductId())));
        assertFalse(products.stream().anyMatch(p -> TestDatabaseHelper.PRODUCT_INACTIVE.equals(p.getSaProductId())));
    }
}
