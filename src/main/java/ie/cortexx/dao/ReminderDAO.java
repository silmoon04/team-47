package ie.cortexx.dao;

import ie.cortexx.model.Reminder;
import java.sql.*;
import java.util.List;

// handles SQL for `reminders` table
public class ReminderDAO {

    public void save(Reminder reminder) throws SQLException {
    }

    public List<Reminder> findByCustomer(int customerId) throws SQLException {
        return null;
    }

    public List<Reminder> findPending() throws SQLException {
        return null;
    }
}