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

    public Sale() {}

    // convenience: walk-in cash sale (defaults: no customer, no discount/vat)
    public Sale(int soldBy, BigDecimal totalAmount, String paymentMethod) {
        this.soldBy = soldBy;
        this.subtotal = totalAmount;
        this.discountAmount = BigDecimal.ZERO;
        this.vatAmount = BigDecimal.ZERO;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.walkIn = true;
    }

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public int getSoldBy() {
        return soldBy;
    }

    public void setSoldBy(int soldBy) {
        this.soldBy = soldBy;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(BigDecimal vatAmount) {
        this.vatAmount = vatAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDateTime saleDate) {
        this.saleDate = saleDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public boolean isWalkIn() {
        return walkIn;
    }

    public void setWalkIn(boolean walkIn) {
        this.walkIn = walkIn;
    }

    public List<SaleItem> getItems() {
        return items;
    }

    public void setItems(List<SaleItem> items) {
        this.items = items;
    }
// TODO: generate getters & setters
}
