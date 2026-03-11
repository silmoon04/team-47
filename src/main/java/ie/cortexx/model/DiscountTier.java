package ie.cortexx.model;

import java.math.BigDecimal;

// maps to `discount_tiers` table
public class DiscountTier {
    private int tierId;
    private String tierName;
    private BigDecimal minMonthlySpend;
    private BigDecimal discountRate;

    // TODO: generate getters & setters
}