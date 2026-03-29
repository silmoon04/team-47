package ie.cortexx.service;

import ie.cortexx.enums.OrderStatus;
import ie.cortexx.exception.*;
import ie.cortexx.interfaces.I_SAtoCA;
import ie.cortexx.model.Order;
import ie.cortexx.model.OrderConfirmation;
import ie.cortexx.model.OrderItem;
import ie.cortexx.model.Product;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// mock implementation of I_SAtoCA for when team A isnt available
// returns hardcoded data matching context/SA-db.md
// swap for real TCP connection on demo day if team A is ready
public class SAProxyService implements I_SAtoCA {

    @Override
    public boolean authenticateMerchant(String username, String password)
            throws IllegalArgumentException {
        // TODO: throw IllegalArgumentException if username/password null or empty
        // TODO: return true if cosymed/bondstreet, false otherwise
        return false;
    }

    @Override
    public List<Product> getCatalogue()
            throws AuthenticationRequiredException, ServiceUnavailableException {
        // TODO: return hardcoded list of 14 products from SA-db.md Catalogue table
        // TODO: throw AuthenticationRequiredException if not authenticated
        return null;
    }

    @Override
    public OrderConfirmation placeOrder(String merchantID, List<OrderItem> items)
            throws IllegalArgumentException, InsufficientStockException, AuthenticationRequiredException {
        // TODO: throw IllegalArgumentException if items null/empty
        // TODO: throw AuthenticationRequiredException if not authenticated
        // TODO: return mock OrderConfirmation with generated id + ACCEPTED status
        return null;
    }

    @Override
    public OrderStatus getOrderStatus(String orderId)
            throws IllegalArgumentException, OrderNotFoundException {
        // TODO: throw IllegalArgumentException if orderId null/empty
        // TODO: throw OrderNotFoundException if unknown id
        // TODO: return mock status (DELIVERED for demo orders)
        return null;
    }

    @Override
    public BigDecimal getOutstandingBalance()
            throws AuthenticationRequiredException, ServiceUnavailableException {
        // TODO: return mock balance (0.00 after scenario 9 payment)
        return null;
    }

    @Override
    public List<Order> getOrderHistory(LocalDate fromDate, LocalDate toDate)
            throws IllegalArgumentException, AuthenticationRequiredException, ServiceUnavailableException {
        // TODO: throw IllegalArgumentException if dates null or fromDate > toDate
        // TODO: return mock orders matching demo scenarios 2 + 4
        return null;
    }
}
