package ie.cortexx.service;

import ie.cortexx.dao.CustomerDAO;
import ie.cortexx.enums.AccountStatus;
import ie.cortexx.model.Customer;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;

public class DebtCycleService {

    private final CustomerDAO customerDAO;

    public DebtCycleService(CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }

    public void checkAndSuspend(int customerId) {
        try {
            var cust = customerDAO.findById(customerId);
            if (cust == null || cust.getAccountStatus() != AccountStatus.NORMAL) return;
            if (cust.getDebtPeriodStart() == null) return;
            if (cust.getDebtPeriodStart().plusDays(14).isBefore(LocalDate.now())) {
                customerDAO.updateAccountStatus(customerId, AccountStatus.SUSPENDED);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void escalateToDefault(int customerId) {
        try {
            var cust = customerDAO.findById(customerId);
            if (cust == null || cust.getAccountStatus() != AccountStatus.SUSPENDED) return;
            if (cust.getDebtPeriodStart() == null) return;
            if (cust.getDebtPeriodStart().plusDays(28).isBefore(LocalDate.now())) {
                customerDAO.updateAccountStatus(customerId, AccountStatus.IN_DEFAULT);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void restoreAccount(int customerId) {
        try {
            var cust = customerDAO.findById(customerId);
            if (cust == null) return;
            if (cust.getOutstandingBalance().compareTo(BigDecimal.ZERO) == 0) {
                customerDAO.updateAccountStatus(customerId, AccountStatus.NORMAL);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}