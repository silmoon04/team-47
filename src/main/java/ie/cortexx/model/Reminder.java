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

    public Reminder() {}

    public Reminder(int customerId, ReminderType reminderType, BigDecimal amountOwed) {
        this.customerId = customerId;
        this.reminderType = reminderType;
        this.amountOwed = amountOwed;
    }

    public int getReminderId() { return reminderId; }
    public void setReminderId(int reminderId) { this.reminderId = reminderId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public ReminderType getReminderType() { return reminderType; }
    public void setReminderType(ReminderType reminderType) { this.reminderType = reminderType; }

    public BigDecimal getAmountOwed() { return amountOwed; }
    public void setAmountOwed(BigDecimal amountOwed) { this.amountOwed = amountOwed; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDate getSentAt() { return sentAt; }
    public void setSentAt(LocalDate sentAt) { this.sentAt = sentAt; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
