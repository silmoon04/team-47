package ie.cortexx.interfaces;

import ie.cortexx.enums.OrderStatus;
import ie.cortexx.exception.*;
import ie.cortexx.model.Order;
import ie.cortexx.model.OrderConfirmation;
import ie.cortexx.model.OrderItem;
import ie.cortexx.model.Product;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// interface provided BY Team A (SA subsystem, team 46)
// we CONSUME this, they give us the implementation
// must match HLD section 7.5.1
public interface I_SAtoCA {

    boolean authenticateMerchant(String username, String password)
        throws IllegalArgumentException;

    List<Product> getCatalogue()
        throws AuthenticationRequiredException, ServiceUnavailableException;

    OrderConfirmation placeOrder(String merchantID, List<OrderItem> items)
        throws IllegalArgumentException, InsufficientStockException, AuthenticationRequiredException;

    OrderStatus getOrderStatus(String orderId)
        throws IllegalArgumentException, OrderNotFoundException;

    BigDecimal getOutstandingBalance()
        throws AuthenticationRequiredException, ServiceUnavailableException;

    List<Order> getOrderHistory(LocalDate fromDate, LocalDate toDate)
        throws IllegalArgumentException, AuthenticationRequiredException, ServiceUnavailableException;
}
