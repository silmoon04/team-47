package ie.cortexx.model;

import java.math.BigDecimal;

// maps to `order_items`, child of Order
public class OrderItem {
    private int orderItemId;
    private int orderId;
    private int productId;
    private int quantity;
    private BigDecimal unitPrice;

    // TODO: generate getters & setters
}