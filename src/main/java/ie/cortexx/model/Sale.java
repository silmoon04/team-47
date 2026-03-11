package ie.cortexx.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// maps to `sales` + `sale_items` tables
public class Sale {
    private int saleId;
    private Integer customerId;
    private int soldBy;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal vatAmount;
    private BigDecimal totalAmount;
    private LocalDateTime saleDate;
    private boolean walkIn;
    private List<SaleItem> items = new ArrayList<>();

    // TODO: generate getters & setters
}