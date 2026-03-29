package ie.cortexx.service;

import ie.cortexx.model.SaleItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

// all the money math lives here, static methods, no state
// always use BigDecimal for money, never double
// all results rounded to 2dp HALF_UP (standard for currency)
public class PriceCalculator {

    // retail = wholesale * (1 + markupRate)
    // e.g. calculateRetailPrice(10.00, 1.00) = 20.00 (100% markup)
    public static BigDecimal calculateRetailPrice(BigDecimal wholesalePrice, BigDecimal markupRate) {
        if (wholesalePrice == null || markupRate == null)
            throw new IllegalArgumentException("price and markup cannot be null");
        return wholesalePrice.add(wholesalePrice.multiply(markupRate))
                .setScale(2, RoundingMode.HALF_UP);
    }

    // vat = price * vatRate
    // e.g. calculateVAT(10.00, 0.23) = 2.30
    public static BigDecimal calculateVAT(BigDecimal price, BigDecimal vatRate) {
        if (price == null || vatRate == null)
            throw new IllegalArgumentException("price and vat rate cannot be null");
        return price.multiply(vatRate).setScale(2, RoundingMode.HALF_UP);
    }

    // lineTotal = quantity * retailPrice
    public static BigDecimal calculateLineTotal(int quantity, BigDecimal retailPrice) {
        if (quantity < 0) throw new IllegalArgumentException("quantity cannot be negative");
        if (retailPrice == null) throw new IllegalArgumentException("retail price cannot be null");
        return retailPrice.multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
    }

    // change = tendered - totalDue (must be >= 0)
    public static BigDecimal calculateChange(BigDecimal tendered, BigDecimal totalDue) {
        if (tendered == null || totalDue == null)
            throw new IllegalArgumentException("amounts cannot be null");
        if (tendered.compareTo(totalDue) < 0)
            throw new IllegalArgumentException("tendered amount less than total due");
        return tendered.subtract(totalDue).setScale(2, RoundingMode.HALF_UP);
    }

    // discount = price * discountRate
    // e.g. calculateDiscountAmount(50.00, 0.10) = 5.00
    public static BigDecimal calculateDiscountAmount(BigDecimal price, BigDecimal discountRate) {
        if (price == null || discountRate == null)
            throw new IllegalArgumentException("price and discount rate cannot be null");
        return price.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
    }

    // total = subtotal + vatAmount
    public static BigDecimal calculateTotalWithVAT(BigDecimal subtotal, BigDecimal vatAmount) {
        if (subtotal == null || vatAmount == null)
            throw new IllegalArgumentException("subtotal and vat cannot be null");
        return subtotal.add(vatAmount).setScale(2, RoundingMode.HALF_UP);
    }

    // convenience: sum all line totals from a list of sale items
    public static BigDecimal calculateSubtotal(List<SaleItem> items) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (SaleItem item : items) {
            subtotal = subtotal.add(calculateLineTotal(item.getQuantity(), item.getUnitPrice()));
        }
        return subtotal.setScale(2, RoundingMode.HALF_UP);
    }
}
