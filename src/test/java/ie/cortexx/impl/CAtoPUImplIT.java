package ie.cortexx.impl;

import ie.cortexx.TestDatabaseHelper;
import ie.cortexx.exception.ProductNotFoundException;
import ie.cortexx.interfaces.I_CAtoPU;
import ie.cortexx.model.StockItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CAtoPUImplIT {

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
    void getStockLevel_validProduct_returnsExactQuantity() {
        I_CAtoPU api = new CAtoPUImpl();

        int level = api.getStockLevel(TestDatabaseHelper.PRODUCT_OK);

        assertEquals(10, level);
    }

    @Test
    void getStockLevel_zeroStockProduct_returnsZero() {
        I_CAtoPU api = new CAtoPUImpl();

        int level = api.getStockLevel(TestDatabaseHelper.PRODUCT_ZERO);

        assertEquals(0, level);
    }

    @Test
    void getStockLevel_nonexistentProduct_throwsProductNotFound() {
        I_CAtoPU api = new CAtoPUImpl();

        assertThrows(ProductNotFoundException.class,
            () -> api.getStockLevel("DOES_NOT_EXIST_999"));
    }

    @Test
    void getStockLevel_nullId_throwsIllegalArgument() {
        I_CAtoPU api = new CAtoPUImpl();

        assertThrows(IllegalArgumentException.class,
            () -> api.getStockLevel(null));
    }

    @Test
    void getStockLevel_emptyId_throwsIllegalArgument() {
        I_CAtoPU api = new CAtoPUImpl();

        assertThrows(IllegalArgumentException.class,
            () -> api.getStockLevel("   "));
    }

    @Test
    void deductStock_sufficientStock_returnsTrue_andUpdatesExactQuantity() {
        I_CAtoPU api = new CAtoPUImpl();

        int before = api.getStockLevel(TestDatabaseHelper.PRODUCT_OK);
        boolean ok = api.deductStock(TestDatabaseHelper.PRODUCT_OK, 3);
        int after = api.getStockLevel(TestDatabaseHelper.PRODUCT_OK);

        assertTrue(ok);
        assertEquals(10, before);
        assertEquals(7, after);
    }

    @Test
    void deductStock_insufficientStock_returnsFalse_andLeavesQuantityUnchanged() {
        I_CAtoPU api = new CAtoPUImpl();

        int before = api.getStockLevel(TestDatabaseHelper.PRODUCT_LOW);
        boolean ok = api.deductStock(TestDatabaseHelper.PRODUCT_LOW, 5);
        int after = api.getStockLevel(TestDatabaseHelper.PRODUCT_LOW);

        assertFalse(ok);
        assertEquals(2, before);
        assertEquals(2, after);
    }

    @Test
    void deductStock_negativeQty_throwsIllegalArgument() {
        I_CAtoPU api = new CAtoPUImpl();

        assertThrows(IllegalArgumentException.class,
            () -> api.deductStock(TestDatabaseHelper.PRODUCT_OK, -1));
    }

    @Test
    void getAllStock_returnsOnlyActiveSeededRows() {
        I_CAtoPU api = new CAtoPUImpl();

        List<StockItem> items = api.getAllStock();

        assertNotNull(items);
        assertEquals(3, items.size());
        assertTrue(items.stream().anyMatch(i -> TestDatabaseHelper.PRODUCT_OK.equals(i.getSaProductId()) && i.getQuantity() == 10));
        assertTrue(items.stream().anyMatch(i -> TestDatabaseHelper.PRODUCT_ZERO.equals(i.getSaProductId()) && i.getQuantity() == 0));
        assertTrue(items.stream().anyMatch(i -> TestDatabaseHelper.PRODUCT_LOW.equals(i.getSaProductId()) && i.getQuantity() == 2));
        assertFalse(items.stream().anyMatch(i -> TestDatabaseHelper.PRODUCT_INACTIVE.equals(i.getSaProductId())));
    }

}
