package ie.cortexx.model;

import ie.cortexx.enums.AccountStatus;
import ie.cortexx.enums.DiscountType;
import ie.cortexx.enums.ReminderStatus;
import java.math.BigDecimal;
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

    // TODO: generate getters & setters
}
