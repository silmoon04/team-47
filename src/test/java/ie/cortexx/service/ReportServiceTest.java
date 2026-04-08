package ie.cortexx.service;

import ie.cortexx.dao.CustomerDAO;
import ie.cortexx.dao.SaleDAO;
import ie.cortexx.dao.StockDAO;
import ie.cortexx.model.Customer;
import ie.cortexx.model.Sale;
import ie.cortexx.model.StockItem;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ReportServiceTest {

    @Test
    void turnover_delegates_to_sale_dao() throws Exception {
        SaleDAO saleDAO = mock(SaleDAO.class);
        StockDAO stockDAO = mock(StockDAO.class);
        CustomerDAO customerDAO = mock(CustomerDAO.class);
        ReportService service = new ReportService(saleDAO, stockDAO, customerDAO);
        when(saleDAO.findByDateRange(LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 7))).thenReturn(List.of(new Sale()));

        assertEquals(1, service.getTurnover(LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 7)).size());
    }

    @Test
    void stock_delegates_to_stock_dao() throws Exception {
        SaleDAO saleDAO = mock(SaleDAO.class);
        StockDAO stockDAO = mock(StockDAO.class);
        CustomerDAO customerDAO = mock(CustomerDAO.class);
        ReportService service = new ReportService(saleDAO, stockDAO, customerDAO);
        when(stockDAO.findAll()).thenReturn(List.of(new StockItem(), new StockItem()));

        assertEquals(2, service.getStock().size());
    }

    @Test
    void debt_summary_delegates_to_customer_dao() throws Exception {
        SaleDAO saleDAO = mock(SaleDAO.class);
        StockDAO stockDAO = mock(StockDAO.class);
        CustomerDAO customerDAO = mock(CustomerDAO.class);
        ReportService service = new ReportService(saleDAO, stockDAO, customerDAO);
        when(customerDAO.findDebtors()).thenReturn(List.of(new Customer()));

        assertEquals(1, service.getDebtSummary().size());
    }
}