package ie.cortexx.model;

import java.math.BigDecimal;

// maps to `discount_tiers`
// glynne's tiers: <£100 = 0%, £100-300 = 1%, £300+ = 2%
public class DiscountTier {
    private int tierId;
    // links tier to a specific customer
    private Integer customerId;
    private String tierName;
    private BigDecimal minMonthlySpend;
    // e.g. 0.0100 = 1%
    private BigDecimal discountRate;

    // TODO: generate getters & setters
}
