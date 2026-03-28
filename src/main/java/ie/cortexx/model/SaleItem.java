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

    public int getSaleItemId() {
        return saleItemId;
    }

    public void setSaleItemId(int saleItemId) {
        this.saleItemId = saleItemId;
    }

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(BigDecimal discountRate) {
        this.discountRate = discountRate;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

// TODO: generate getters & setters

}
