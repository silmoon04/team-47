package ie.cortexx.dao;

import ie.cortexx.model.Reminder;
import java.sql.*;
import java.util.List;

// handles SQL for `reminders` table
// always scoped to a customer (WHERE customer_id = ?)
// debt cycle service creates reminders, then marks them sent
public class ReminderDAO {

    // TODO: SELECT * FROM reminders WHERE customer_id = ?
    public List<Reminder> findByCustomer(int customerId) throws SQLException {
        return null;
    }

    // TODO: INSERT INTO reminders (...) VALUES (...) with RETURN_GENERATED_KEYS
    public void save(Reminder reminder) throws SQLException {
    }

    // TODO: UPDATE reminders SET sent_at = CURDATE() WHERE reminder_id = ?
    public void markSent(int reminderId) throws SQLException {
    }
}
