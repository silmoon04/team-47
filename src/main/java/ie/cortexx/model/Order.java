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

    // TODO: generate getters & setters
}
