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

    public int getTierId() { return tierId; }
    public void setTierId(int tierId) { this.tierId = tierId; }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public String getTierName() { return tierName; }
    public void setTierName(String tierName) { this.tierName = tierName; }

    public BigDecimal getMinMonthlySpend() { return minMonthlySpend; }
    public void setMinMonthlySpend(BigDecimal minMonthlySpend) { this.minMonthlySpend = minMonthlySpend; }

    public BigDecimal getDiscountRate() { return discountRate; }
    public void setDiscountRate(BigDecimal discountRate) { this.discountRate = discountRate; }
}
