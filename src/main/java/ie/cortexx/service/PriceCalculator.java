package ie.cortexx.service;

// all the money math lives here, static methods, no state
// always use BigDecimal for money, never double
//
// why BigDecimal? because 0.1 + 0.2 = 0.30000000000000004 with double
// BigDecimal does exact decimal math
public class PriceCalculator {

    // TODO: calculateRetailPrice(wholesale, markupRate) -> BigDecimal
    // TODO: calculateVAT(price, vatRate) -> BigDecimal
    // TODO: calculateLineTotal(quantity, retailPrice) -> BigDecimal
    // TODO: calculateChange(tendered, totalDue) -> BigDecimal
    // TODO: calculateDiscountAmount(price, discountRate) -> BigDecimal
    // TODO: calculateTotalWithVAT(subtotal, vatAmount) -> BigDecimal
}
