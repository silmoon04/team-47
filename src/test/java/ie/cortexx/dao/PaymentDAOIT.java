package ie.cortexx.dao;

import ie.cortexx.enums.PaymentType;
import ie.cortexx.model.Payment;
import org.junit.jupiter.api.Test;

import static ie.cortexx.TestDatabaseHelper.bd;
import static ie.cortexx.TestDatabaseHelper.del;
import static ie.cortexx.TestDatabaseHelper.id;
import static org.junit.jupiter.api.Assertions.*;

class PaymentDAOIT {

    @Test
    void findBySaleReturnsSavedCashPayment() throws Exception {
        int saleId = id("SELECT sale_id FROM sales LIMIT 1");
        var dao = new PaymentDAO();
        var payment = new Payment(saleId, PaymentType.CASH, bd("7.50"));

        dao.save(payment);

        try {
            var list = dao.findBySale(saleId);
            assertNotNull(list);
            assertTrue(list.stream().anyMatch(x -> x.getPaymentId() == payment.getPaymentId()));
        } finally {
            del("payments", "payment_id", payment.getPaymentId());
        }
    }

    @Test
    void findByCustomerReturnsStandaloneAccountPayment() throws Exception {
        int customerId = id("SELECT customer_id FROM customers LIMIT 1");
        var dao = new PaymentDAO();
        var payment = new Payment();
        payment.setCustomerId(customerId);
        payment.setPaymentType(PaymentType.ACCOUNT_PAYMENT);
        payment.setAmount(bd("9.00"));
        payment.setChangeGiven(bd("0.00"));

        dao.save(payment);

        try {
            var list = dao.findByCustomer(customerId);
            assertNotNull(list);
            assertTrue(list.stream().anyMatch(x -> x.getPaymentId() == payment.getPaymentId()));
        } finally {
            del("payments", "payment_id", payment.getPaymentId());
        }
    }

    @Test
    void saveCardPaymentPreservesMaskedCardFields() throws Exception {
        int saleId = id("SELECT sale_id FROM sales LIMIT 1");
        var dao = new PaymentDAO();
        var payment = new Payment();
        payment.setSaleId(saleId);
        payment.setPaymentType(PaymentType.CREDIT_CARD);
        payment.setAmount(bd("11.40"));
        payment.setCardType("Visa");
        payment.setCardFirst4("4111");
        payment.setCardLast4("2222");
        payment.setCardExpiry("12/29");
        payment.setChangeGiven(bd("0.00"));

        dao.save(payment);

        try {
            Payment saved = dao.findBySale(saleId).stream()
                .filter(x -> x.getPaymentId() == payment.getPaymentId())
                .findFirst()
                .orElseThrow();

            assertEquals("Visa", saved.getCardType());
            assertEquals("4111", saved.getCardFirst4());
            assertEquals("2222", saved.getCardLast4());
            assertEquals("12/29", saved.getCardExpiry());
        } finally {
            del("payments", "payment_id", payment.getPaymentId());
        }
    }
}