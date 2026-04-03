package ie.cortexx.service;

import ie.cortexx.enums.PaymentType;
import ie.cortexx.model.Customer;
import ie.cortexx.model.SaleItem;


import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


public class SaleService {

    // TODO: add constructor, inject needed DAOs
    // TODO: implement service methods

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



}
