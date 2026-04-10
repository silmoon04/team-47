package ie.cortexx.service;

import ie.cortexx.dao.CustomerDAO;
import ie.cortexx.dao.DiscountTierDAO;
import ie.cortexx.dao.PaymentDAO;
import ie.cortexx.enums.AccountStatus;
import ie.cortexx.enums.PaymentType;
import ie.cortexx.model.Customer;
import ie.cortexx.model.DiscountTier;
import ie.cortexx.model.Payment;
import ie.cortexx.util.DBConnection;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

// CRUD for credit account customers
public class CustomerService {

    @FunctionalInterface
    public interface TransactionRunner {
        <T> T execute(DBConnection.TransactionWork<T> work) throws SQLException;
    }

    private final CustomerDAO customerDAO;
    private final PaymentDAO paymentDAO;
    private final DiscountTierDAO discountTierDAO;
    private final TransactionRunner transactionRunner;

    public CustomerService() {
        this(new CustomerDAO(), new PaymentDAO(), new DiscountTierDAO(), DBConnection::withTransaction);
    }

    public CustomerService(CustomerDAO customerDAO, PaymentDAO paymentDAO, DiscountTierDAO discountTierDAO) {
        this(customerDAO, paymentDAO, discountTierDAO, DBConnection::withTransaction);
    }

    public CustomerService(CustomerDAO customerDAO, PaymentDAO paymentDAO, DiscountTierDAO discountTierDAO, TransactionRunner transactionRunner) {
        this.customerDAO = customerDAO;
        this.paymentDAO = paymentDAO;
        this.discountTierDAO = discountTierDAO;
        this.transactionRunner = transactionRunner;
    }

    public List<Customer> findAll() throws SQLException {
        return customerDAO.findAll();
    }

    public Customer findById(int customerId) throws SQLException {
        return customerDAO.findById(customerId);
    }

    public List<DiscountTier> findTiers(int customerId) throws SQLException {
        return discountTierDAO.findByCustomer(customerId);
    }

    public void save(Customer customer) throws SQLException {
        customerDAO.save(customer);
    }

    public void update(Customer customer) throws SQLException {
        customerDAO.update(customer);
    }

    public void delete(int customerId) throws SQLException {
        customerDAO.delete(customerId);
    }

    public Customer receivePayment(int customerId, BigDecimal amount, PaymentType paymentType) throws SQLException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than 0");
        }
        if (paymentType == null) {
            throw new IllegalArgumentException("Payment type is required");
        }
        if (paymentType != PaymentType.ACCOUNT_PAYMENT) {
            throw new IllegalArgumentException("Payment type must be account payment");
        }

        return transactionRunner.execute(connection -> {
            Customer customer = customerDAO.findById(connection, customerId);
            if (customer == null) {
                return null;
            }
            if (amount.compareTo(customer.getOutstandingBalance()) > 0) {
                throw new IllegalArgumentException("Payment exceeds outstanding balance");
            }

            Payment payment = new Payment();
            payment.setCustomerId(customerId);
            payment.setPaymentType(paymentType);
            payment.setAmount(amount);
            payment.setChangeGiven(BigDecimal.ZERO);
            paymentDAO.save(connection, payment);

            BigDecimal newBalance = customer.getOutstandingBalance().subtract(amount);
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                newBalance = BigDecimal.ZERO;
            }

            customer.setOutstandingBalance(newBalance);
            customer.setLastPaymentDate(LocalDate.now());
            if (newBalance.compareTo(BigDecimal.ZERO) == 0 && customer.getAccountStatus() != AccountStatus.IN_DEFAULT) {
                customer.setAccountStatus(AccountStatus.NORMAL);
            }
            customerDAO.update(connection, customer);
            return customer;
        });
    }
}