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

    // TODO: generate getters & setters
}