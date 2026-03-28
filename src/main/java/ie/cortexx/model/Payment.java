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

    // TODO: generate getters & setters
}
