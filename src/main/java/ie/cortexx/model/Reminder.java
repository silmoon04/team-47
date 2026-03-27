package ie.cortexx.model;

import ie.cortexx.enums.ReminderType;
import java.math.BigDecimal;
import java.time.LocalDate;

// maps to `reminders`
// demo scenario 16: generate reminders on 15 apr and 29 apr for all debtors (eva)
public class Reminder {
    private int reminderId;
    private int customerId;
    // ENUM in schema: FIRST or SECOND
    private ReminderType reminderType;
    private BigDecimal amountOwed;
    private LocalDate dueDate;
    // nullable, unsent reminders have no date
    private LocalDate sentAt;
    // TEXT in schema, the actual letter content from template
    private String content;

    // TODO: generate getters & setters
}
