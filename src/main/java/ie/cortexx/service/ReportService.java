package ie.cortexx.service;

import ie.cortexx.dao.CustomerDAO;
import ie.cortexx.dao.SaleDAO;
import ie.cortexx.dao.StockDAO;
import ie.cortexx.model.Customer;
import ie.cortexx.model.Sale;
import ie.cortexx.model.StockItem;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

// aggregates data for sales/stock/customer reports
public class ReportService {
    private final SaleDAO saleDAO;
    private final StockDAO stockDAO;
    private final CustomerDAO customerDAO;

    public ReportService() {
        this(new SaleDAO(), new StockDAO(), new CustomerDAO());
    }

    public ReportService(SaleDAO saleDAO, StockDAO stockDAO, CustomerDAO customerDAO) {
        this.saleDAO = saleDAO;
        this.stockDAO = stockDAO;
        this.customerDAO = customerDAO;
    }

    public List<Sale> getTurnover(LocalDate from, LocalDate to) throws SQLException {
        return saleDAO.findByDateRange(from, to);
    }

    public List<StockItem> getStock() throws SQLException {
        return stockDAO.findAll();
    }

    public List<Customer> getDebtSummary() throws SQLException {
        return customerDAO.findDebtors();
    }
}