package ie.cortexx.service;

import ie.cortexx.dao.StockDAO;
import ie.cortexx.model.StockItem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class StockServiceTest {

    @Test
    void add_stock_delegates_to_dao() throws Exception {
        StockDAO stockDAO = mock(StockDAO.class);
        StockService service = new StockService(stockDAO);

        service.addStock(5, 10);

        verify(stockDAO).updateQuantity(5, 10);
    }

    @Test
    void find_low_stock_delegates_to_dao() throws Exception {
        StockDAO stockDAO = mock(StockDAO.class);
        StockService service = new StockService(stockDAO);
        when(stockDAO.findLowStock()).thenReturn(List.of(new StockItem(), new StockItem()));

        assertEquals(2, service.findLowStock().size());
    }
}