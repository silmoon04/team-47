package ie.cortexx.dao;

import ie.cortexx.model.Sale;
import ie.cortexx.model.SaleItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static ie.cortexx.TestDatabaseHelper.*;
import static org.junit.jupiter.api.Assertions.*;

class SaleDAOIT {

    @Test
    void savePersistsItemsAndGeneratedId() throws Exception {
        SaleDAO dao = new SaleDAO();
        Sale sale = sale();

        dao.save(sale);

        try {
            assertTrue(sale.getSaleId() > 0);
            assertEquals(1, dao.countItems(sale.getSaleId()));
        } finally {
            del("sales", "sale_id", sale.getSaleId());
        }
    }

    @Test
    void findByCustomerAndDateRangeReturnSavedSale() throws Exception {
        SaleDAO dao = new SaleDAO();
        Sale sale = sale();
        sale.setCustomerId(id("SELECT customer_id FROM customers LIMIT 1"));
        sale.setWalkIn(false);
        sale.setPaymentMethod("ON_CREDIT");
        dao.save(sale);

        try {
            assertTrue(dao.findByCustomer(sale.getCustomerId()).stream().anyMatch(x -> x.getSaleId() == sale.getSaleId()));
            assertTrue(dao.findByDateRange(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1)).stream().anyMatch(x -> x.getSaleId() == sale.getSaleId()));
        } finally {
            del("sales", "sale_id", sale.getSaleId());
        }
    }

    private Sale sale() throws Exception {
        int userId = id("SELECT user_id FROM users LIMIT 1");
        int productId = id("SELECT product_id FROM products LIMIT 1");
        String productName = str("SELECT name FROM products WHERE product_id = " + productId);

        Sale sale = new Sale();
        sale.setSoldBy(userId);
        sale.setSubtotal(bd("9.99"));
        sale.setDiscountAmount(BigDecimal.ZERO);
        sale.setVatAmount(BigDecimal.ZERO);
        sale.setTotalAmount(bd("9.99"));
        sale.setSaleDate(LocalDateTime.now());
        sale.setPaymentMethod("CASH");
        sale.setWalkIn(true);
        sale.getItems().add(new SaleItem(productId, productName, 1, bd("9.99"), bd("9.99")));
        return sale;
    }
}