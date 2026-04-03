package ie.cortexx.service;

import ie.cortexx.enums.PaymentType;
import ie.cortexx.model.*;
import ie.cortexx.dao.SaleDAO;
import ie.cortexx.dao.StockDAO;
import ie.cortexx.dao.PaymentDAO;



import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SaleService {

    // TODO: add constructor, inject needed DAOs
    // TODO: implement service methods

    private final SaleDAO saleDAO;
    private final StockDAO stockDAO;
    private final PaymentDAO paymentDAO;

    public SaleService(SaleDAO saleDAO, StockDAO stockDAO, PaymentDAO paymentDAO) {
        this.saleDAO = saleDAO;
        this.stockDAO = stockDAO;
        this.paymentDAO = paymentDAO;
    }

   //rule 1 no stock

    public ValidationResult validateStock(List<SaleItem> items, Map<Integer, Integer> stockLevels){
        for (SaleItem item : items) {
            int available = stockLevels.getOrDefault(item.getProductId(), 0);
            if (available < item.getQuantity()) {
                return ValidationResult.fail( "Item"+item.getProductName()+"is out of stock" );
            }
        }
        return ValidationResult.ok();

    }

    //rule 2 only walk in cash

    public ValidationResult validatePaymentType(Customer customer, PaymentType paymentType, boolean isWalkIn) {
        if (!isWalkIn && customer != null && paymentType == PaymentType.CASH) {
            return ValidationResult.fail("YOu cant pay with cash if you have an account.");
        }
        return ValidationResult.ok();
    }

    //rule 3 cant exceed credit limti

    public ValidationResult validateCreditLimit(Customer customer, PaymentType paymentType, BigDecimal grandTotal) {
        if (paymentType == PaymentType.ON_CREDIT) {
            BigDecimal newBalance = customer.getOutstandingBalance().add(grandTotal);
            if (newBalance.compareTo(customer.getCreditLimit()) > 0) {
                return ValidationResult.fail("exceeds credit limit of £" + customer.getCreditLimit());
            }
        }
        return ValidationResult.ok();
    }

    //rule4 walk in has to pay in full
    public ValidationResult validateWalkIn(boolean isWalkIn, PaymentType paymentType) {
        if (isWalkIn && paymentType == PaymentType.ON_CREDIT) {
            return ValidationResult.fail("walk ins have to pay in full"
            );
        }
        return ValidationResult.ok();
    }

    public ValidationResult processSale(Sale sale, Payment payment) {
        BigDecimal grandTotal = payment.getAmount();
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

        try{
            saleDAO.save(sale);

            for(SaleItem item : sale.getItems()) {
                int deduct = -item.getQuantity();
                stockDAO.updateQuantity(item.getProductId(), deduct);
            }
            paymentDAO.save(payment);
        } catch(SQLException error) {
            return ValidationResult.fail(error.getMessage());
        }
        return ValidationResult.ok();



    }





}
