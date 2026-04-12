package ie.cortexx.service;

import ie.cortexx.enums.PaymentType;
import ie.cortexx.model.*;
import ie.cortexx.dao.SaleDAO;
import ie.cortexx.dao.StockDAO;
import ie.cortexx.dao.PaymentDAO;
import ie.cortexx.dao.CustomerDAO;
import ie.cortexx.dao.DiscountTierDAO;
import ie.cortexx.enums.AccountStatus;
import ie.cortexx.util.DBConnection;



import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SaleService {

    @FunctionalInterface
    public interface TransactionRunner extends DBConnection.TransactionRunner {
    }

    private final SaleDAO saleDAO;
    private final StockDAO stockDAO;
    private final PaymentDAO paymentDAO;
    private final CustomerDAO customerDAO;
    private final DiscountService discountService;
    private final TransactionRunner transactionRunner;

    public SaleService() {
        this(new SaleDAO(), new StockDAO(), new PaymentDAO(), new CustomerDAO(), DBConnection::withTransaction);
    }

    public SaleService(SaleDAO saleDAO, StockDAO stockDAO, PaymentDAO paymentDAO, CustomerDAO customerDAO) {
        this(saleDAO, stockDAO, paymentDAO, customerDAO, DBConnection::withTransaction);
    }

    public SaleService(SaleDAO saleDAO, StockDAO stockDAO, PaymentDAO paymentDAO, CustomerDAO customerDAO, TransactionRunner transactionRunner) {
        this.saleDAO = saleDAO;
        this.stockDAO = stockDAO;
        this.paymentDAO = paymentDAO;
        this.customerDAO = customerDAO;
        this.discountService = new DiscountService(saleDAO, new DiscountTierDAO());
        this.transactionRunner = transactionRunner;
    }

    public ValidationResult validateStock(List<SaleItem> items, Map<Integer, Integer> stockLevels){
        if (items == null || items.isEmpty()) {
            return ValidationResult.fail("Sale needs at least one item");
        }
        for (SaleItem item : items) {
            if (item.getQuantity() <= 0) {
                return ValidationResult.fail("Item " + item.getProductName() + " has invalid qty");
            }
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

    public ValidationResult validatePayment(Payment payment) {
        if (payment == null || payment.getPaymentType() == null) {
            return ValidationResult.fail("Payment type is required");
        }
        if (payment.getPaymentType() == PaymentType.ACCOUNT_PAYMENT) {
            return ValidationResult.fail("Account payment is not valid for checkout");
        }
        if (payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return ValidationResult.fail("Payment amount must be greater than 0");
        }
        return ValidationResult.ok();
    }

    public ValidationResult processSale(Sale sale, Payment payment) {
        ValidationResult paymentValidation = validatePayment(payment);
        if (!paymentValidation.isValid()) {
            return paymentValidation;
        }

        if (sale.getTotalAmount() == null) {
            return ValidationResult.fail("Sale total amount is required");
        }

        // use the sale total for credit
        BigDecimal grandTotal = sale.getTotalAmount();
        PaymentType paymentType = payment.getPaymentType();

        if (paymentType != PaymentType.ON_CREDIT
            && payment.getAmount() != null
            && payment.getAmount().compareTo(grandTotal) < 0) {
            return ValidationResult.fail("Payment amount must cover sale total");
        }

        Map<Integer, Integer> stockLevels = new HashMap<>();
        try{
            for (SaleItem item : sale.getItems()) {
                StockItem stock = stockDAO.findByProductId(item.getProductId());
                int quantity = 0;
                if (stock != null) {
                    quantity = stock.getQuantity();
                }
                stockLevels.put(item.getProductId(), quantity);
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
            try {
                BigDecimal discount = discountService.resolveDiscount(customer, sale.getSubtotal(),
                    sale.getSaleDate() != null ? sale.getSaleDate().toLocalDate() : java.time.LocalDate.now());
                if (discount.compareTo(BigDecimal.ZERO) > 0) {
                    sale.setDiscountAmount(discount);
                    grandTotal = sale.getSubtotal().subtract(discount).add(sale.getVatAmount() != null ? sale.getVatAmount() : BigDecimal.ZERO);
                    sale.setTotalAmount(grandTotal);
                }
            } catch (SQLException ignored) { }

            ValidationResult creditCheck = validateCreditLimit(customer, paymentType, grandTotal);
            if (!creditCheck.isValid()) {
                return creditCheck;
            }
        }

        Customer resolvedCustomer = customer;
        BigDecimal resolvedGrandTotal = grandTotal;
        PaymentType resolvedPaymentType = paymentType;


        try{
            transactionRunner.execute(connection -> {
                saleDAO.save(connection, sale);
                payment.setSaleId(sale.getSaleId());

                for (SaleItem item : sale.getItems()) {
                    boolean deducted = stockDAO.tryDeductQuantity(connection, item.getProductId(), item.getQuantity());
                    if (!deducted) {
                        throw new SQLException("Item " + item.getProductName() + " is out of stock");
                    }
                }

                if (resolvedPaymentType == PaymentType.ON_CREDIT) {
                    payment.setAmount(resolvedGrandTotal);
                }
                if (resolvedCustomer != null) {
                    payment.setCustomerId(resolvedCustomer.getCustomerId());
                }
                paymentDAO.save(connection, payment);

                if (resolvedPaymentType == PaymentType.ON_CREDIT && resolvedCustomer != null) {
                    BigDecimal newBalance = resolvedCustomer.getOutstandingBalance().add(resolvedGrandTotal);
                    customerDAO.updateBalance(connection, resolvedCustomer.getCustomerId(), newBalance);
                }

                return null;
            });
        } catch(SQLException error) {
            sale.setSaleId(0);
            payment.setSaleId(0);
            payment.setPaymentId(0);
            return ValidationResult.fail(error.getMessage());
        }
        return ValidationResult.ok();
    }
}
