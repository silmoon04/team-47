package ie.cortexx.model;

import java.time.LocalDateTime;

// maps to `stock` table (joined with products usually)
public class StockItem {
    private int stockId;
    private int productId;
    private String productName;
    private int quantity;
    private int reorderLevel;
    private LocalDateTime lastUpdated;

    // TODO: generate getters & setters
    // TODO: maybe add isLowStock() helper
}