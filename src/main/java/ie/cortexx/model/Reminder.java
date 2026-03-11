package ie.cortexx.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// maps to `reminders` table
public class Reminder {
    private int reminderId;
    private int customerId;
    private String reminderType;
    private BigDecimal amountOwed;
    private LocalDate dueDate;
    private LocalDateTime sentAt;
    private String content;

    // TODO: generate getters & setters
}