package ie.cortexx.model;

import ie.cortexx.enums.PaymentType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// maps to `payments` table
public class Payment {
    private int paymentId;
    private Integer saleId;
    private Integer customerId;
    private PaymentType paymentType;
    private BigDecimal amount;
    private String cardType;
    private String cardFirst4;
    private String cardLast4;
    private String cardExpiry;
    private BigDecimal changeGiven;
    private LocalDateTime paymentDate;

    // TODO: generate getters & setters
}