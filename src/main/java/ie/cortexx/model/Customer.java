package ie.cortexx.model;

import ie.cortexx.enums.AccountStatus;
import ie.cortexx.enums.DiscountType;
import ie.cortexx.enums.ReminderStatus;
import ie.cortexx.enums.UserRole;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

// maps to `customers` table
// demo: eva bauyer (ACC0001, FIXED 3%), glynne morrison (ACC0002, FLEXIBLE tiers)
public class Customer {

    private int customerId;
    // ACC0001, ACC0002 etc
    private String accountNo;
    private String name;
    // can differ from name, e.g. company contact
    private String contactName;
    private String email;
    private String phone;
    private String address;
    private AccountStatus accountStatus;
    // both demo customers have £500 limit
    private BigDecimal creditLimit;
    private BigDecimal outstandingBalance;
    // FIXED for eva (3%), FLEXIBLE for glynne (tiered)
    private DiscountType discountType;
    // null for FLEXIBLE customers
    private BigDecimal fixedDiscountRate;
    // null for FIXED customers, links to discount_tiers
    private Integer flexibleTierId;
    // null for new customers with no debt
    private LocalDate debtPeriodStart;
    private LocalDate lastPaymentDate;
    // split from the old single reminder_dates column
    private LocalDate date1stReminder;
    private ReminderStatus status1stReminder;
    private LocalDate date2ndReminder;
    private ReminderStatus status2ndReminder;
    private LocalDateTime createdAt;
    // nullable, admin-created customers at setup have no user
    private Integer createdBy;
    // DEFAULT 1
    private int merchantId;

    public Customer() {}

    // convenience: name, address, discount type (defaults: NORMAL status, £500 limit, merchant 1)
    public Customer(String name, String address, DiscountType discountType) {
        this.name = name;
        this.address = address;
        this.discountType = discountType;
        this.accountStatus = AccountStatus.NORMAL;
        this.creditLimit = new BigDecimal("500.00");
        this.outstandingBalance = BigDecimal.ZERO;
        this.merchantId = 1;
    }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getAccountNo() { return accountNo; }
    public void setAccountNo(String accountNo) { this.accountNo = accountNo; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public AccountStatus getAccountStatus() { return accountStatus; }
    public void setAccountStatus(AccountStatus accountStatus) { this.accountStatus = accountStatus; }

    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }

    public BigDecimal getOutstandingBalance() { return outstandingBalance; }
    public void setOutstandingBalance(BigDecimal outstandingBalance) { this.outstandingBalance = outstandingBalance; }

    public DiscountType getDiscountType() { return discountType; }
    public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }

    public BigDecimal getFixedDiscountRate() { return fixedDiscountRate; }
    public void setFixedDiscountRate(BigDecimal fixedDiscountRate) { this.fixedDiscountRate = fixedDiscountRate; }

    public Integer getFlexibleTierId() { return flexibleTierId; }
    public void setFlexibleTierId(Integer flexibleTierId) { this.flexibleTierId = flexibleTierId; }

    public LocalDate getDebtPeriodStart() { return debtPeriodStart; }
    public void setDebtPeriodStart(LocalDate debtPeriodStart) { this.debtPeriodStart = debtPeriodStart; }

    public LocalDate getLastPaymentDate() { return lastPaymentDate; }
    public void setLastPaymentDate(LocalDate lastPaymentDate) { this.lastPaymentDate = lastPaymentDate; }

    public LocalDate getDate1stReminder() { return date1stReminder; }
    public void setDate1stReminder(LocalDate date1stReminder) { this.date1stReminder = date1stReminder; }

    public ReminderStatus getStatus1stReminder() { return status1stReminder; }
    public void setStatus1stReminder(ReminderStatus status1stReminder) { this.status1stReminder = status1stReminder; }

    public LocalDate getDate2ndReminder() { return date2ndReminder; }
    public void setDate2ndReminder(LocalDate date2ndReminder) { this.date2ndReminder = date2ndReminder; }

    public ReminderStatus getStatus2ndReminder() { return status2ndReminder; }
    public void setStatus2ndReminder(ReminderStatus status2ndReminder) { this.status2ndReminder = status2ndReminder; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

    public int getMerchantId() { return merchantId; }
    public void setMerchantId(int merchantId) { this.merchantId = merchantId; }

    // builds a User from a ResultSet row so DAOs dont need a separate mapUser()
    // usage in DAO: return User.fromRS(rs);
    public static Customer CfromRS(ResultSet rs) throws SQLException {
        Customer c = new Customer();

        c.customerId = rs.getInt("customer_id");
        c.accountNo = rs.getString("account_no");
        c.name = rs.getString("name");
        c.contactName = rs.getString("contact_name");
        c.email = rs.getString("email");
        c.phone = rs.getString("phone");
        c.address = rs.getString("address");
        c.accountStatus = AccountStatus.valueOf(rs.getString("account_status"));
        c.creditLimit = rs.getBigDecimal("credit_limit");
        c.outstandingBalance = rs.getBigDecimal("outstanding_balance");
        c.discountType = DiscountType.valueOf(rs.getString("discount_type"));
        c.fixedDiscountRate = rs.getBigDecimal("fixed_discount_rate");
        c.flexibleTierId = rs.getInt("flexible_tier_id");
        c.date1stReminder = rs.getDate("date_1st_reminder").toLocalDate( );
        c.status1stReminder = ReminderStatus.valueOf(rs.getString("status_1st_reminder"));
        c.date2ndReminder = rs.getDate("date_2nd_reminder").toLocalDate( );
        c.status2ndReminder = ReminderStatus.valueOf(rs.getString("status_2nd_reminder"));
        c.createdAt = rs.getTimestamp("created_at").toLocalDateTime();
        c.createdBy = rs.getInt("created_by");


        return c;
    }
}
