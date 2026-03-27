package ie.cortexx.model;

import java.math.BigDecimal;

// maps to `sale_items`, child of Sale
// ON DELETE CASCADE so deleting a sale cleans up its items
public class SaleItem {
    private int saleItemId;
    private int saleId;
    private int productId;
    // stored so receipt still works if product name changes later
    private String productName;
    private int quantity;
    // retail price at time of sale
    private BigDecimal unitPrice;
    // e.g. 0.0300 = 3% for eva
    private BigDecimal discountRate;
    private BigDecimal lineTotal;

    // TODO: generate getters & setters
}
