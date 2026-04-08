package ie.cortexx.service;

import ie.cortexx.dao.CustomerDAO;
import ie.cortexx.dao.DiscountTierDAO;
import ie.cortexx.dao.PaymentDAO;
import ie.cortexx.enums.AccountStatus;
import ie.cortexx.enums.PaymentType;
import ie.cortexx.model.Customer;
import ie.cortexx.model.DiscountTier;
import ie.cortexx.model.Payment;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

// CRUD for credit account customers
public class CustomerService {
    private final CustomerDAO customerDAO;
    private final PaymentDAO paymentDAO;
    private final DiscountTierDAO discountTierDAO;

    public CustomerService() {
        this(new CustomerDAO(), new PaymentDAO(), new DiscountTierDAO());
    }

    public CustomerService(CustomerDAO customerDAO, PaymentDAO paymentDAO, DiscountTierDAO discountTierDAO) {
        this.customerDAO = customerDAO;
        this.paymentDAO = paymentDAO;
        this.discountTierDAO = discountTierDAO;
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
        Customer customer = customerDAO.findById(customerId);
        if (customer == null) {
            return null;
        }

        Payment payment = new Payment();
        payment.setCustomerId(customerId);
        payment.setPaymentType(paymentType);
        payment.setAmount(amount);
        payment.setChangeGiven(BigDecimal.ZERO);
        paymentDAO.save(payment);

        BigDecimal newBalance = customer.getOutstandingBalance().subtract(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            newBalance = BigDecimal.ZERO;
        }

        customer.setOutstandingBalance(newBalance);
        customer.setLastPaymentDate(LocalDate.now());
        if (newBalance.compareTo(BigDecimal.ZERO) == 0) {
            customer.setAccountStatus(AccountStatus.NORMAL);
        }
        customerDAO.update(customer);
        return customer;
    }
}