package ie.cortexx.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// maps to `stock` (joined with products usually)
// demo: lipitor is AT reorder level (10/10), rhynol is BELOW (14/15)
public class StockItem {
    private int stockId;
    private int productId;
    // not in stock table, comes from JOIN with products
    private String productName;

    // added three new fields based on SA db
    private String saProductId;
    private BigDecimal costPrice;
    private BigDecimal markupRate;

    private int quantity;
    // INT DEFAULT 10 in schema
    private int reorderLevel;
    private LocalDateTime lastUpdated;

    public StockItem() {}

    public StockItem(int productId, int quantity, int reorderLevel) {
        this.productId = productId;
        this.quantity = quantity;
        this.reorderLevel = reorderLevel;
    }

    public boolean isLowStock() { return quantity <= reorderLevel; }

    public int getStockId() { return stockId; }
    public void setStockId(int stockId) { this.stockId = stockId; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getSaProductId() { return saProductId; }
    public void setSaProductId(String saProductId) { this.saProductId = saProductId; }
    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
    public BigDecimal getMarkupRate() { return markupRate; }
    public void setMarkupRate(BigDecimal markupRate) { this.markupRate = markupRate; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
