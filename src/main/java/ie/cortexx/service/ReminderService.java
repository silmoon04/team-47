package ie.cortexx.service;

import ie.cortexx.dao.CustomerDAO;
import ie.cortexx.dao.ReminderDAO;
import ie.cortexx.enums.ReminderType;
import ie.cortexx.model.Reminder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReminderService {

    private final CustomerDAO customerDAO;
    private final ReminderDAO reminderDAO;

    public ReminderService(CustomerDAO customerDAO, ReminderDAO reminderDAO) {
        this.customerDAO = customerDAO;
        this.reminderDAO = reminderDAO;
    }

    public List<Reminder> generateReminders() {
        var created = new ArrayList<Reminder>();
        try {
            for (var cust : customerDAO.findDebtors()) {
                var r = new Reminder(cust.getCustomerId(), ReminderType.FIRST, cust.getOutstandingBalance());
                reminderDAO.save(r);
                created.add(r);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return created;
    }

    public void markSent(int reminderId) {
        try {
            reminderDAO.markSent(reminderId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}