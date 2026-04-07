package ie.cortexx.dao;

import ie.cortexx.TestDatabaseHelper;
import ie.cortexx.model.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

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
    void findAll_returnsOnlyActiveSeededProducts() throws Exception {
        ProductDAO dao = new ProductDAO();
        List<Product> products = dao.findAll();

        assertNotNull(products);
        assertEquals(3, products.size());

        assertTrue(products.stream().allMatch(Product::isActive));
        assertTrue(products.stream().anyMatch(p -> TestDatabaseHelper.PRODUCT_OK.equals(p.getSaProductId())));
        assertTrue(products.stream().anyMatch(p -> TestDatabaseHelper.PRODUCT_ZERO.equals(p.getSaProductId())));
        assertTrue(products.stream().anyMatch(p -> TestDatabaseHelper.PRODUCT_LOW.equals(p.getSaProductId())));
        assertFalse(products.stream().anyMatch(p -> TestDatabaseHelper.PRODUCT_INACTIVE.equals(p.getSaProductId())));
    }
}
