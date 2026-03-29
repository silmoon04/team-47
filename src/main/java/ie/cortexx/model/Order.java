package ie.cortexx.model;

import ie.cortexx.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// maps to `orders` + `order_items`
// demo: scenario 2 (25 feb, £376) and scenario 4 (10 mar, £430), both delivered
public class Order {

    private int orderId;
    // string id from team A
    private String saOrderId;
    // INT DEFAULT 1 in schema
    private int merchantId;
    private OrderStatus orderStatus;
    private BigDecimal totalAmount;
    private LocalDateTime orderedAt;
    // nullable, undelivered orders have no date
    private LocalDateTime deliveredAt;
    private int orderedBy;
    private List<OrderItem> items = new ArrayList<>();

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getSaOrderId() { return saOrderId; }
    public void setSaOrderId(String saOrderId) { this.saOrderId = saOrderId; }

    public int getMerchantId() { return merchantId; }
    public void setMerchantId(int merchantId) { this.merchantId = merchantId; }

    public OrderStatus getOrderStatus() { return orderStatus; }
    public void setOrderStatus(OrderStatus orderStatus) { this.orderStatus = orderStatus; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public LocalDateTime getOrderedAt() { return orderedAt; }
    public void setOrderedAt(LocalDateTime orderedAt) { this.orderedAt = orderedAt; }

    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }

    public int getOrderedBy() { return orderedBy; }
    public void setOrderedBy(int orderedBy) { this.orderedBy = orderedBy; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}
