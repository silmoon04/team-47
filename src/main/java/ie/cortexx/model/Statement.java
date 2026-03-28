package ie.cortexx.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// maps to `statements`
// shows purchases/payments for an account holder over a period
public class Statement {
    private int statementId;
    private int customerId;
    // DATE in schema, not DATETIME
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal openingBalance;
    private BigDecimal totalPurchases;
    private BigDecimal totalPayments;
    private BigDecimal closingBalance;
    private LocalDateTime generatedAt;
    private int generatedBy;

    // TODO: generate getters & setters
}
