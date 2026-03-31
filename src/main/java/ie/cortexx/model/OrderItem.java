package ie.cortexx.model;

import java.math.BigDecimal;

// maps to `order_items`, child of Order
// ON DELETE CASCADE so deleting an order cleans up its items
public class OrderItem {

    private int orderItemId;
    private int orderId;
    private int productId;
    private int quantity;
    private BigDecimal unitPrice;

    public OrderItem() {}

    public OrderItem(int productId, int quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public int getOrderItemId() { return orderItemId; }
    public void setOrderItemId(int orderItemId) { this.orderItemId = orderItemId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}
