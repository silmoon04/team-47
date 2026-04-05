package ie.cortexx.service;

import ie.cortexx.dao.CustomerDAO;
import ie.cortexx.dao.PaymentDAO;
import ie.cortexx.dao.SaleDAO;
import ie.cortexx.dao.StockDAO;
import ie.cortexx.dao.UserDAO;
import ie.cortexx.enums.PaymentType;
import ie.cortexx.enums.UserRole;
import ie.cortexx.model.Customer;
import ie.cortexx.model.SaleItem;
import ie.cortexx.model.User;
import ie.cortexx.util.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// task-gate tests
// run only when you want to check a teammate branch:
// mvn test -DtaskGate.service=true -Dtest=TaskGateServiceTest
@EnabledIfSystemProperty(named = "taskGate.service", matches = "true")
class TaskGateServiceTest {

    @AfterEach
    void clearSession() {
        SessionManager.getInstance().logout();
    }

    @Test
    void authServiceAuthenticateLogsInUser() throws Exception {
        var userDao = mock(UserDAO.class);
        var user = new User("sysdba", "masterkey", "System Administrator", UserRole.ADMIN);
        user.setUserId(1);

        when(userDao.authenticate("sysdba", "masterkey")).thenReturn(user);

        var auth = AuthService.class.getDeclaredConstructor(UserDAO.class).newInstance(userDao);
        Method authenticate = AuthService.class.getDeclaredMethod("authenticate", String.class, String.class);

        boolean ok = (boolean) authenticate.invoke(auth, "sysdba", "masterkey");

        assertTrue(ok);
        assertTrue(SessionManager.getInstance().isLoggedIn());
        assertEquals("sysdba", SessionManager.getInstance().getCurrentUser().getUsername());
    }

    @Test
    void authServiceLogoutClearsSession() throws Exception {
        var userDao = mock(UserDAO.class);
        var user = new User("manager", "Get_it_done", "Manager", UserRole.MANAGER);
        user.setUserId(2);

        when(userDao.authenticate("manager", "Get_it_done")).thenReturn(user);

        var auth = AuthService.class.getDeclaredConstructor(UserDAO.class).newInstance(userDao);
        Method authenticate = AuthService.class.getDeclaredMethod("authenticate", String.class, String.class);
        Method logout = AuthService.class.getDeclaredMethod("logout");

        assertTrue((boolean) authenticate.invoke(auth, "manager", "Get_it_done"));
        assertTrue(SessionManager.getInstance().isLoggedIn());

        logout.invoke(auth);

        assertFalse(SessionManager.getInstance().isLoggedIn());
        assertNull(SessionManager.getInstance().getCurrentUser());
    }

    @Test
    void authServiceHashPasswordReturnsString() throws Exception {
        assertEquals(String.class,
            AuthService.class.getDeclaredMethod("hashPassword", String.class).getReturnType());
    }

    @Test
    void saleServiceValidateStockRejectsInsufficientQuantity() throws Exception {
        Object service = newSaleServiceInstance();
        Method validateStock = SaleService.class.getDeclaredMethod("validateStock", List.class, Map.class);

        var item = new SaleItem(1, "test", 3, bd("1.00"), bd("3.00"));
        ValidationResult result = (ValidationResult) validateStock.invoke(service, List.of(item), Map.of(1, 2));

        assertFalse(result.isValid());
    }

    @Test
    void saleServiceValidateWalkInRejectsOnCredit() throws Exception {
        Object service = newSaleServiceInstance();
        Method validateWalkIn = SaleService.class.getDeclaredMethod("validateWalkIn", boolean.class, PaymentType.class);

        ValidationResult result = (ValidationResult) validateWalkIn.invoke(service, true, PaymentType.ON_CREDIT);

        assertFalse(result.isValid());
    }

    @Test
    void saleServiceValidateCreditLimitRejectsOverLimit() throws Exception {
        Object service = newSaleServiceInstance();
        Method validateCreditLimit = SaleService.class.getDeclaredMethod(
            "validateCreditLimit", Customer.class, PaymentType.class, BigDecimal.class);

        var customer = new Customer();
        customer.setOutstandingBalance(bd("490.00"));
        customer.setCreditLimit(bd("500.00"));

        ValidationResult result = (ValidationResult) validateCreditLimit.invoke(
            service, customer, PaymentType.ON_CREDIT, bd("20.00"));

        assertFalse(result.isValid());
    }

    private Object newSaleServiceInstance() throws Exception {
        try {
            Constructor<SaleService> ctor = SaleService.class.getDeclaredConstructor();
            return ctor.newInstance();
        } catch (NoSuchMethodException ignored) {
            Constructor<SaleService> ctor = SaleService.class.getDeclaredConstructor(
                SaleDAO.class, StockDAO.class, PaymentDAO.class, CustomerDAO.class);
            return ctor.newInstance(
                mock(SaleDAO.class),
                mock(StockDAO.class),
                mock(PaymentDAO.class),
                mock(CustomerDAO.class)
            );
        }
    }

    private BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
