package ie.cortexx.model;

import java.math.BigDecimal;

// maps to `sale_items` table, child of Sale
public class SaleItem {
    private int saleItemId;
    private int saleId;
    private int productId;
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountRate;
    private BigDecimal lineTotal;

    // TODO: generate getters & setters
}