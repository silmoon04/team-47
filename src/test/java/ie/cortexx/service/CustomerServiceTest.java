package ie.cortexx.service;

import ie.cortexx.dao.CustomerDAO;
import ie.cortexx.dao.DiscountTierDAO;
import ie.cortexx.dao.PaymentDAO;
import ie.cortexx.enums.AccountStatus;
import ie.cortexx.enums.PaymentType;
import ie.cortexx.model.Customer;
import ie.cortexx.model.DiscountTier;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerServiceTest {

    @Test
    void receive_payment_clamps_balance_and_restores_normal_status() throws Exception {
        CustomerDAO customerDAO = mock(CustomerDAO.class);
        PaymentDAO paymentDAO = mock(PaymentDAO.class);
        DiscountTierDAO discountTierDAO = mock(DiscountTierDAO.class);
        CustomerService service = new CustomerService(customerDAO, paymentDAO, discountTierDAO);

        Customer customer = new Customer();
        customer.setCustomerId(3);
        customer.setOutstandingBalance(new BigDecimal("10.00"));
        customer.setAccountStatus(AccountStatus.SUSPENDED);
        when(customerDAO.findById(3)).thenReturn(customer);

        Customer updated = service.receivePayment(3, new BigDecimal("15.00"), PaymentType.ACCOUNT_PAYMENT);

        assertNotNull(updated);
        assertEquals(new BigDecimal("0.00"), updated.getOutstandingBalance().setScale(2));
        assertEquals(AccountStatus.NORMAL, updated.getAccountStatus());
        verify(paymentDAO).save(any());
        verify(customerDAO).update(customer);
    }

    @Test
    void find_tiers_delegates_to_dao() throws Exception {
        CustomerDAO customerDAO = mock(CustomerDAO.class);
        PaymentDAO paymentDAO = mock(PaymentDAO.class);
        DiscountTierDAO discountTierDAO = mock(DiscountTierDAO.class);
        CustomerService service = new CustomerService(customerDAO, paymentDAO, discountTierDAO);
        when(discountTierDAO.findByCustomer(4)).thenReturn(List.of(new DiscountTier()));

        assertEquals(1, service.findTiers(4).size());
    }

    @Test
    void delete_delegates_to_dao() throws Exception {
        CustomerDAO customerDAO = mock(CustomerDAO.class);
        PaymentDAO paymentDAO = mock(PaymentDAO.class);
        DiscountTierDAO discountTierDAO = mock(DiscountTierDAO.class);
        CustomerService service = new CustomerService(customerDAO, paymentDAO, discountTierDAO);

        service.delete(9);

        verify(customerDAO).delete(9);
    }
}