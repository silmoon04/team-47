package ie.cortexx.model;

import ie.cortexx.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// maps to `orders` + `order_items` tables
public class Order {
    private int orderId;
    private String saOrderId;
    private String merchantId;
    private OrderStatus orderStatus;
    private BigDecimal totalAmount;
    private LocalDateTime orderedAt;
    private LocalDateTime deliveredAt;
    private int orderedBy;
    private List<OrderItem> items = new ArrayList<>();

    // TODO: generate getters & setters
}