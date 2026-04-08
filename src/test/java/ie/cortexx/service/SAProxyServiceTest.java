package ie.cortexx.service;

import ie.cortexx.exception.AuthenticationRequiredException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SAProxyServiceTest {

    private final SAProxyService service = new SAProxyService();

    @Test
    void authenticate_rejects_blank_username() {
        assertThrows(IllegalArgumentException.class, () -> service.authenticateMerchant("", "pw"));
    }

    @Test
    void catalogue_requires_authentication() {
        assertThrows(AuthenticationRequiredException.class, service::getCatalogue);
    }

    @Test
    void balance_requires_authentication() {
        assertThrows(AuthenticationRequiredException.class, service::getOutstandingBalance);
    }

    @Test
    void history_rejects_invalid_dates_before_db_access() {
        assertThrows(AuthenticationRequiredException.class, () -> service.getOrderHistory(LocalDate.now(), LocalDate.now().minusDays(1)));
    }

    @Test
    void place_order_rejects_empty_items_after_auth_check() {
        assertThrows(AuthenticationRequiredException.class, () -> service.placeOrder("ACC-1002", java.util.List.of()));
    }

    @Test
    void order_status_rejects_blank_id_after_auth_check() {
        assertThrows(AuthenticationRequiredException.class, () -> service.getOrderStatus(""));
    }
}