package ie.cortexx.service;

import ie.cortexx.enums.PaymentType;
import ie.cortexx.model.*;
import ie.cortexx.dao.SaleDAO;
import ie.cortexx.dao.StockDAO;
import ie.cortexx.dao.PaymentDAO;
import ie.cortexx.dao.CustomerDAO;
import ie.cortexx.enums.AccountStatus;



import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SaleService {

    private final SaleDAO saleDAO;
    private final StockDAO stockDAO;
    private final PaymentDAO paymentDAO;
    private final CustomerDAO customerDAO;

    public SaleService() {
        this(new SaleDAO(), new StockDAO(), new PaymentDAO(), new CustomerDAO());
    }

    public SaleService(SaleDAO saleDAO, StockDAO stockDAO, PaymentDAO paymentDAO, CustomerDAO customerDAO) {
        this.saleDAO = saleDAO;
        this.stockDAO = stockDAO;
        this.paymentDAO = paymentDAO;
        this.customerDAO = customerDAO;
    }

    public ValidationResult validateStock(List<SaleItem> items, Map<Integer, Integer> stockLevels){
        for (SaleItem item : items) {
            int available = stockLevels.getOrDefault(item.getProductId(), 0);
            if (available < item.getQuantity()) {
                return ValidationResult.fail("Item " + item.getProductName() + " is out of stock");
            }
        }
        return ValidationResult.ok();

    }

    public ValidationResult validatePaymentType(Customer customer, PaymentType paymentType, boolean isWalkIn) {
        if (!isWalkIn && customer != null && paymentType == PaymentType.CASH) {
            return ValidationResult.fail("You cannot pay cash for an account sale.");
        }
        return ValidationResult.ok();
    }

    public ValidationResult validateCreditLimit(Customer customer, PaymentType paymentType, BigDecimal grandTotal) {
        if (paymentType == PaymentType.ON_CREDIT) {
            BigDecimal newBalance = customer.getOutstandingBalance().add(grandTotal);
            if (newBalance.compareTo(customer.getCreditLimit()) > 0) {
                return ValidationResult.fail("Exceeds credit limit of £" + customer.getCreditLimit());
            }
        }
        return ValidationResult.ok();
    }

    public ValidationResult validateWalkIn(boolean isWalkIn, PaymentType paymentType) {
        if (isWalkIn && paymentType == PaymentType.ON_CREDIT) {
            return ValidationResult.fail("Walk-ins have to pay in full");
        }
        return ValidationResult.ok();
    }

    public ValidationResult processSale(Sale sale, Payment payment) {
        // use the sale total for credit
        BigDecimal grandTotal = sale.getTotalAmount() != null ? sale.getTotalAmount() : payment.getAmount();
        PaymentType paymentType = payment.getPaymentType();

        Map<Integer, Integer> stockLevels = new HashMap<>();
        try{
            for (SaleItem item : sale.getItems()) {
                StockItem stock = stockDAO.findByProductId(item.getProductId());
                int quanity = 0;
                if (stock != null) {
                    quanity = stock.getQuantity();
                }
                stockLevels.put(item.getProductId(), quanity);
            }
        } catch(SQLException error) {
            return ValidationResult.fail(error.getMessage());
        }

        ValidationResult stockCheck = validateStock(sale.getItems(), stockLevels);
        if (!stockCheck.isValid()) {
            return stockCheck;
        }

        ValidationResult walkInCheck = validateWalkIn(sale.isWalkIn(), paymentType);
        if (!walkInCheck.isValid()) {
            return walkInCheck;
        }
        Customer customer = null;
        if (!sale.isWalkIn() && sale.getCustomerId() != null) {
            try{
                customer= customerDAO.findById(sale.getCustomerId());
            } catch(SQLException error) {
                return ValidationResult.fail(error.getMessage());
            }
        }

        if (!sale.isWalkIn() && customer == null) {
            return ValidationResult.fail("Account sale needs a customer");
        }

        if (customer != null) {
            if(customer.getAccountStatus() == AccountStatus.SUSPENDED){
                return ValidationResult.fail("Account is suspended");
            }
            if (customer.getAccountStatus() ==AccountStatus.IN_DEFAULT){
                return ValidationResult.fail("Account is in default");
            }
        }

        ValidationResult paymentCheck = validatePaymentType(customer, paymentType, sale.isWalkIn());
        if (!paymentCheck.isValid()) {
            return paymentCheck;
        }

        if (customer != null) {
            ValidationResult creditCheck = validateCreditLimit(customer, paymentType, grandTotal);
            if (!creditCheck.isValid()) {
                return creditCheck;
            }
        }


        try{
            saleDAO.save(sale);
            payment.setSaleId(sale.getSaleId());

            for(SaleItem item : sale.getItems()) {
                int deduct = -item.getQuantity();
                stockDAO.updateQuantity(item.getProductId(), deduct);
            }

            if (paymentType == PaymentType.ON_CREDIT) {
                payment.setAmount(grandTotal);
            }
            paymentDAO.save(payment);

            if (paymentType == PaymentType.ON_CREDIT && customer != null) {
                BigDecimal newBalance = customer.getOutstandingBalance().add(grandTotal);
                customerDAO.updateBalance(customer.getCustomerId(), newBalance);
            }

        } catch(SQLException error) {
            return ValidationResult.fail(error.getMessage());
        }
        return ValidationResult.ok();
    }
}
