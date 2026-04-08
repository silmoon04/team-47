package ie.cortexx.service;

import ie.cortexx.dao.CustomerDAO;
import ie.cortexx.dao.PaymentDAO;
import ie.cortexx.dao.SaleDAO;
import ie.cortexx.dao.StockDAO;
import ie.cortexx.enums.AccountStatus;
import ie.cortexx.enums.PaymentType;
import ie.cortexx.model.Customer;
import ie.cortexx.model.Payment;
import ie.cortexx.model.Sale;
import ie.cortexx.model.SaleItem;
import ie.cortexx.model.StockItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static ie.cortexx.TestDatabaseHelper.bd;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock private SaleDAO saleDAO;
    @Mock private StockDAO stockDAO;
    @Mock private PaymentDAO paymentDAO;
    @Mock private CustomerDAO customerDAO;

    private SaleService saleService;

    @BeforeEach
    void setUp() {
        saleService = new SaleService(saleDAO, stockDAO, paymentDAO, customerDAO);
    }

    @Test
    void validate_stock_rejects_insufficient_quantity() {
        SaleItem item = new SaleItem(1, "Paracetamol", 2, bd("5.00"), bd("10.00"));

        ValidationResult result = saleService.validateStock(List.of(item), Map.of(1, 1));

        assertFalse(result.isValid());
    }

    @Test
    void validate_walk_in_rejects_credit() {
        ValidationResult result = saleService.validateWalkIn(true, PaymentType.ON_CREDIT);
        assertFalse(result.isValid());
    }

    @Test
    void process_sale_walk_in_cash_succeeds() throws Exception {
        when(stockDAO.findByProductId(1)).thenReturn(stock(1, 5));

        Sale sale = sale(true, null, bd("10.00"));
        Payment payment = payment(PaymentType.CASH, bd("10.00"));

        ValidationResult result = saleService.processSale(sale, payment);

        assertTrue(result.isValid());
        verify(saleDAO).save(sale);
        verify(paymentDAO).save(payment);
        verify(stockDAO).updateQuantity(1, -1);
    }

    @Test
    void process_sale_rejects_suspended_customer() throws Exception {
        when(stockDAO.findByProductId(1)).thenReturn(stock(1, 5));
        when(customerDAO.findById(7)).thenReturn(customer(AccountStatus.SUSPENDED, bd("0.00"), bd("500.00")));

        ValidationResult result = saleService.processSale(sale(false, 7, bd("12.00")), payment(PaymentType.ON_CREDIT, bd("12.00")));

        assertFalse(result.isValid());
        verifyNoInteractions(saleDAO);
    }

    @Test
    void process_sale_rejects_credit_limit_overflow() throws Exception {
        when(stockDAO.findByProductId(1)).thenReturn(stock(1, 5));
        when(customerDAO.findById(7)).thenReturn(customer(AccountStatus.NORMAL, bd("495.00"), bd("500.00")));

        ValidationResult result = saleService.processSale(sale(false, 7, bd("12.00")), payment(PaymentType.ON_CREDIT, bd("12.00")));

        assertFalse(result.isValid());
        verifyNoInteractions(paymentDAO);
    }

    @Test
    void process_sale_updates_balance_for_credit_sale() throws Exception {
        Customer customer = customer(AccountStatus.NORMAL, bd("10.00"), bd("500.00"));
        when(stockDAO.findByProductId(1)).thenReturn(stock(1, 5));
        when(customerDAO.findById(7)).thenReturn(customer);

        ValidationResult result = saleService.processSale(sale(false, 7, bd("12.00")), payment(PaymentType.ON_CREDIT, bd("12.00")));

        assertTrue(result.isValid());
        verify(customerDAO).updateBalance(7, bd("22.00"));
    }

    private Sale sale(boolean walkIn, Integer customerId, BigDecimal total) {
        Sale sale = new Sale();
        sale.setCustomerId(customerId);
        sale.setSoldBy(1);
        sale.setSubtotal(total);
        sale.setDiscountAmount(BigDecimal.ZERO);
        sale.setVatAmount(BigDecimal.ZERO);
        sale.setTotalAmount(total);
        sale.setSaleDate(LocalDateTime.now());
        sale.setPaymentMethod(walkIn ? "CASH" : "ON_CREDIT");
        sale.setWalkIn(walkIn);
        sale.getItems().add(new SaleItem(1, "Paracetamol", 1, total, total));
        return sale;
    }

    private Payment payment(PaymentType type, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setPaymentType(type);
        payment.setAmount(amount);
        payment.setChangeGiven(BigDecimal.ZERO);
        return payment;
    }

    private StockItem stock(int productId, int quantity) {
        StockItem item = new StockItem();
        item.setProductId(productId);
        item.setQuantity(quantity);
        return item;
    }

    private Customer customer(AccountStatus status, BigDecimal balance, BigDecimal limit) {
        Customer customer = new Customer();
        customer.setCustomerId(7);
        customer.setAccountStatus(status);
        customer.setOutstandingBalance(balance);
        customer.setCreditLimit(limit);
        return customer;
    }

}