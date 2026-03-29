package ie.cortexx.model;

import ie.cortexx.enums.PaymentType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// maps to `payments`
// demo: glynne pays full balance with credit card (scenario 14),
// scenario 11 has cash + card walk-in payments
public class Payment {
    private int paymentId;
    // null for account payments not tied to a specific sale
    private Integer saleId;
    // null for walk-in cash/card
    private Integer customerId;
    private PaymentType paymentType;
    private BigDecimal amount;
    // nullable, 'Visa', 'Mastercard' etc
    private String cardType;
    // CHAR(4) nullable, cash payments have no card info
    private String cardFirst4;
    private String cardLast4;
    // nullable, e.g. "12/2028"
    private String cardExpiry;
    // DEFAULT 0.00, only non-zero for cash
    private BigDecimal changeGiven;
    private LocalDateTime paymentDate;

    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }
    public int getSaleId() { return saleId != null ? saleId : 0; }
    public void setSaleId(int saleId) { this.saleId = saleId; }
    public int getCustomerId() { return customerId != null ? customerId : 0; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public PaymentType getPaymentType() { return paymentType; }
    public void setPaymentType(PaymentType paymentType) { this.paymentType = paymentType; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }
    public String getCardFirst4() { return cardFirst4; }
    public void setCardFirst4(String cardFirst4) { this.cardFirst4 = cardFirst4; }
    public String getCardLast4() { return cardLast4; }
    public void setCardLast4(String cardLast4) { this.cardLast4 = cardLast4; }
    public String getCardExpiry() { return cardExpiry; }
    public void setCardExpiry(String cardExpiry) { this.cardExpiry = cardExpiry; }
    public BigDecimal getChangeGiven() { return changeGiven; }
    public void setChangeGiven(BigDecimal changeGiven) { this.changeGiven = changeGiven; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
}
