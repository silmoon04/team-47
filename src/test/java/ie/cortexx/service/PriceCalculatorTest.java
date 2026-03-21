package ie.cortexx.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PriceCalculatorTest {

    @Test
    void calculateRetailPrice_appliesMarkup() {
        BigDecimal retailPrice = PriceCalculator.calculateRetailPrice(
            new BigDecimal("10.00"),
            new BigDecimal("0.20")
        );

        assertEquals(new BigDecimal("12.00"), retailPrice);
    }

    @Test
    void calculateVAT_appliesVatRate() {
        BigDecimal vat = PriceCalculator.calculateVAT(
            new BigDecimal("10.00"),
            new BigDecimal("0.23")
        );

        assertEquals(new BigDecimal("2.30"), vat);
    }

    @Test
    void calculateLineTotal_multipliesQuantityByRetailPrice() {
        BigDecimal lineTotal = PriceCalculator.calculateLineTotal(
            5,
            new BigDecimal("12.00")
        );

        assertEquals(new BigDecimal("60.00"), lineTotal);
    }

    @Test
    void calculateChange_returnsDifferenceBetweenTenderedAndTotalDue() {
        BigDecimal change = PriceCalculator.calculateChange(
            new BigDecimal("20.00"),
            new BigDecimal("15.50")
        );

        assertEquals(new BigDecimal("4.50"), change);
    }

    @Test
    void calculateDiscountAmount_appliesDiscountRate() {
        BigDecimal discount = PriceCalculator.calculateDiscountAmount(
            new BigDecimal("50.00"),
            new BigDecimal("0.10")
        );

        assertEquals(new BigDecimal("5.00"), discount);
    }

    @Test
    void calculateTotalWithVAT_addsSubtotalAndVat() {
        BigDecimal total = PriceCalculator.calculateTotalWithVAT(
            new BigDecimal("12.00"),
            new BigDecimal("2.76")
        );

        assertEquals(new BigDecimal("14.76"), total);
    }

    @Test
    void calculateLineTotal_rejectsNegativeQuantity() {
        assertThrows(
            IllegalArgumentException.class,
            () -> PriceCalculator.calculateLineTotal(-1, new BigDecimal("12.00"))
        );
    }

    @Test
    void calculateRetailPrice_rejectsNullWholesale() {
        assertThrows(
            IllegalArgumentException.class,
            () -> PriceCalculator.calculateRetailPrice(null, new BigDecimal("0.20"))
        );
    }

    @Test
    void calculateChange_rejectsInsufficientTenderedAmount() {
        assertThrows(
            IllegalArgumentException.class,
            () -> PriceCalculator.calculateChange(
                new BigDecimal("10.00"),
                new BigDecimal("15.50")
            )
        );
    }
}
