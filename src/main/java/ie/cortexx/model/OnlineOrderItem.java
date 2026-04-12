package ie.cortexx.model;

import java.math.BigDecimal;

// maps to `online_order_items`, the CA mirror of Team C PU order lines
public class OnlineOrderItem {

    private int onlineOrderItemId;
    private int onlineOrderId;
    private int productId;
    private int quantity;
    private BigDecimal unitPrice;

    public int getOnlineOrderItemId() { return onlineOrderItemId; }
    public void setOnlineOrderItemId(int onlineOrderItemId) { this.onlineOrderItemId = onlineOrderItemId; }

    public int getOnlineOrderId() { return onlineOrderId; }
    public void setOnlineOrderId(int onlineOrderId) { this.onlineOrderId = onlineOrderId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}