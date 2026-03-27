package ie.cortexx.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// maps to `sales` + `sale_items`
// demo scenarios 10-13: eva/glynne credit sales, walk-in cash/card sales
public class Sale {
    private int saleId;
    // null for walk-in customers
    private Integer customerId;
    private int soldBy;
    private BigDecimal subtotal;
    // DEFAULT 0.00, only non-zero for account holders with discount plans
    private BigDecimal discountAmount;
    // DEFAULT 0.00, 0% vat for demo
    private BigDecimal vatAmount;
    private BigDecimal totalAmount;
    private LocalDateTime saleDate;
    // nullable, VARCHAR(20): 'CASH', 'CREDIT_CARD' etc
    private String paymentMethod;
    // false for account holder purchases
    private boolean walkIn;
    private List<SaleItem> items = new ArrayList<>();

    // TODO: generate getters & setters
}
