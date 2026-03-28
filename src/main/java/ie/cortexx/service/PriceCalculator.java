package ie.cortexx.service;

import ie.cortexx.model.SaleItem;

import java.math.BigDecimal;
import java.util.List;

// all the money math lives here, static methods, no state
// always use BigDecimal for money, never double
//
// why BigDecimal? because 0.1 + 0.2 = 0.30000000000000004 with double
// BigDecimal does exact decimal math
public class PriceCalculator {

    // TODO: calculateRetailPrice(wholesale, markupRate) -> BigDecimal //done
    // TODO: calculateVAT(price, vatRate) -> BigDecimal //done
    // TODO: calculateLineTotal(quantity, retailPrice) -> BigDecimal //done
    // TODO: calculateChange(tendered, totalDue) -> BigDecimal //done
    // TODO: calculateDiscountAmount(price, discountRate) -> BigDecimal //done
    // TODO: calculateTotalWithVAT(subtotal, vatAmount) -> BigDecimal //done

    //test commit
    private static final BigDecimal VAT_RATE = new BigDecimal("0.23");

    public static BigDecimal calculateLineTotal(int quantity, BigDecimal retailPrice){
        BigDecimal lineTotal = retailPrice.multiply(BigDecimal.valueOf(quantity));
        return lineTotal;
    }

    public static BigDecimal calculateSubtotal(List<SaleItem> items) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (SaleItem item : items) {
            BigDecimal lineTotal = calculateLineTotal(item.getQuantity(), item.getUnitPrice());

            subtotal = subtotal.add(lineTotal);
        }
        return subtotal;

    }

    public static BigDecimal calculateRetailPrice(BigDecimal wholesalePrice, BigDecimal markupRate){
        BigDecimal markup = wholesalePrice.multiply(markupRate);
        BigDecimal retailPrice = wholesalePrice.add(markup);
        return retailPrice;
    }

    public static BigDecimal calculateDiscountAmount(BigDecimal price, BigDecimal discountRate) {
        BigDecimal discount = price.multiply(discountRate);
        return discount;


    }

    public static BigDecimal calculateVAT(BigDecimal price, BigDecimal vatRate ){
    BigDecimal vat= price.multiply(vatRate);
    return vat;
    }

    public static BigDecimal calculateTotalWithVAT(BigDecimal subtotal, BigDecimal vatAmount) {
        BigDecimal totalWithVAT = subtotal.add(vatAmount);
        return totalWithVAT;
    }
    public static BigDecimal calculateChange(BigDecimal tendered, BigDecimal totalDue){
    BigDecimal change = tendered.subtract(totalDue);
    return change;
    }



}
