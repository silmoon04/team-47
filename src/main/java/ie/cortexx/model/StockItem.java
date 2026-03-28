package ie.cortexx.model;

import java.time.LocalDateTime;

// maps to `stock` (joined with products usually)
// demo: lipitor is AT reorder level (10/10), rhynol is BELOW (14/15)
public class StockItem {
    private int stockId;
    private int productId;
    // not in stock table, comes from JOIN with products
    private String productName;
    private int quantity;
    // INT DEFAULT 10 in schema
    private int reorderLevel;
    private LocalDateTime lastUpdated;

    // TODO: generate getters & setters
    // TODO: isLowStock() helper, quantity <= reorderLevel
}
