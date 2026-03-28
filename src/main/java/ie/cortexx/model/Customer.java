package ie.cortexx.model;

import ie.cortexx.enums.AccountStatus;
import ie.cortexx.enums.DiscountType;
import ie.cortexx.enums.ReminderStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// maps to `customers` table
// this is central to the debt cycle stuff (UC25-UC36)
public class Customer {
    private int customerId;
    private String name;
    private String email;
    private String phone;
    private String address;
    private AccountStatus accountStatus;
    private BigDecimal creditLimit;
    private BigDecimal outstandingBalance;
    private DiscountType discountType;
    private BigDecimal fixedDiscountRate;
    private Integer flexibleTierId;
    private LocalDate debtPeriodStart;
    private LocalDate date1stReminder;
    private ReminderStatus status1stReminder;
    private LocalDate date2ndReminder;
    private ReminderStatus status2ndReminder;
    private LocalDateTime createdAt;
    private int createdBy;

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public BigDecimal getOutstandingBalance() {
        return outstandingBalance;
    }

    public void setOutstandingBalance(BigDecimal outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public void setDiscountType(DiscountType discountType) {
        this.discountType = discountType;
    }

    public BigDecimal getFixedDiscountRate() {
        return fixedDiscountRate;
    }

    public void setFixedDiscountRate(BigDecimal fixedDiscountRate) {
        this.fixedDiscountRate = fixedDiscountRate;
    }

    public Integer getFlexibleTierId() {
        return flexibleTierId;
    }

    public void setFlexibleTierId(Integer flexibleTierId) {
        this.flexibleTierId = flexibleTierId;
    }

    public LocalDate getDebtPeriodStart() {
        return debtPeriodStart;
    }

    public void setDebtPeriodStart(LocalDate debtPeriodStart) {
        this.debtPeriodStart = debtPeriodStart;
    }

    public LocalDate getDate1stReminder() {
        return date1stReminder;
    }

    public void setDate1stReminder(LocalDate date1stReminder) {
        this.date1stReminder = date1stReminder;
    }

    public ReminderStatus getStatus1stReminder() {
        return status1stReminder;
    }

    public void setStatus1stReminder(ReminderStatus status1stReminder) {
        this.status1stReminder = status1stReminder;
    }

    public LocalDate getDate2ndReminder() {
        return date2ndReminder;
    }

    public void setDate2ndReminder(LocalDate date2ndReminder) {
        this.date2ndReminder = date2ndReminder;
    }

    public ReminderStatus getStatus2ndReminder() {
        return status2ndReminder;
    }

    public void setStatus2ndReminder(ReminderStatus status2ndReminder) {
        this.status2ndReminder = status2ndReminder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }
// TODO: generate getters & setters
}
