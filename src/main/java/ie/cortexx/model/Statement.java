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

    public Statement() {}

    public Statement(int customerId, LocalDate periodStart, LocalDate periodEnd,
                     BigDecimal openingBalance, BigDecimal closingBalance, int generatedBy) {
        this.customerId = customerId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.openingBalance = openingBalance;
        this.totalPurchases = BigDecimal.ZERO;
        this.totalPayments = BigDecimal.ZERO;
        this.closingBalance = closingBalance;
        this.generatedBy = generatedBy;
    }

    public int getStatementId() { return statementId; }
    public void setStatementId(int statementId) { this.statementId = statementId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }

    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }

    public BigDecimal getOpeningBalance() { return openingBalance; }
    public void setOpeningBalance(BigDecimal openingBalance) { this.openingBalance = openingBalance; }

    public BigDecimal getTotalPurchases() { return totalPurchases; }
    public void setTotalPurchases(BigDecimal totalPurchases) { this.totalPurchases = totalPurchases; }

    public BigDecimal getTotalPayments() { return totalPayments; }
    public void setTotalPayments(BigDecimal totalPayments) { this.totalPayments = totalPayments; }

    public BigDecimal getClosingBalance() { return closingBalance; }
    public void setClosingBalance(BigDecimal closingBalance) { this.closingBalance = closingBalance; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public int getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(int generatedBy) { this.generatedBy = generatedBy; }
}
