package ie.cortexx.dao;

import ie.cortexx.enums.ReminderType;
import ie.cortexx.model.Reminder;
import ie.cortexx.util.DBConnection;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

// handles SQL for `reminders` table
// always scoped to a customer (WHERE customer_id = ?)
// debt cycle service creates reminders, then marks them sent
public class ReminderDAO {

    // SELECT * FROM reminders WHERE customer_id = ?
    public List<Reminder> findByCustomer(int customerId) throws SQLException {
        String sql = "SELECT * FROM reminders WHERE customer_id = ? ORDER BY reminder_id DESC";
        List<Reminder> reminders = new ArrayList<>();

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setInt(1, customerId);

            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    reminders.add(mapReminder(rs));
                }
            }
        }

        return reminders;
    }

    // INSERT INTO reminders (...) VALUES (...) with RETURN_GENERATED_KEYS
    public void save(Reminder reminder) throws SQLException {
        String sql = "INSERT INTO reminders (customer_id, reminder_type, amount_owed, due_date, sent_at, content) "
            + "VALUES (?, ?, ?, ?, ?, ?)";

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, reminder.getCustomerId());
            ps.setString(2, reminder.getReminderType().name());

            ps.setBigDecimal(3, reminder.getAmountOwed());

            if (reminder.getDueDate() != null) {
                ps.setDate(4, Date.valueOf(reminder.getDueDate()));
            } else {
                ps.setNull(4, Types.DATE);
            }

            if (reminder.getSentAt() != null) {
                ps.setDate(5, Date.valueOf(reminder.getSentAt()));
            } else {
                ps.setNull(5, Types.DATE);
            }

            ps.setString(6, reminder.getContent());

            ps.executeUpdate();

            try (var rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    reminder.setReminderId(rs.getInt(1));
                }
            }
        }
    }

    public void markSent(int reminderId) throws SQLException {
        String sql = "UPDATE reminders SET sent_at = CURDATE() WHERE reminder_id = ?";

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setInt(1, reminderId);
            ps.executeUpdate();
        }
    }
    private Reminder mapReminder(ResultSet rs) throws SQLException {
        Reminder r = new Reminder();
        r.setReminderId(rs.getInt("reminder_id"));
        r.setCustomerId(rs.getInt("customer_id"));
        r.setReminderType(ReminderType.valueOf(rs.getString("reminder_type")));
        r.setAmountOwed(rs.getBigDecimal("amount_owed"));

        Date dueDate = rs.getDate("due_date");
        if (dueDate != null) {
            r.setDueDate(dueDate.toLocalDate());
        }

        Date sentAt = rs.getDate("sent_at");
        if (sentAt != null) {
            r.setSentAt(sentAt.toLocalDate());
        }

        r.setContent(rs.getString("content"));
        return r;
    }
}
