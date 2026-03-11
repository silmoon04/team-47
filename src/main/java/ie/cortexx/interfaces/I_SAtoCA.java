package ie.cortexx.interfaces;

import ie.cortexx.model.OrderConfirmation;
import ie.cortexx.model.Product;
import java.util.List;

// interface provided BY Team A (SA subsystem)
// we CONSUME this — they give us the implementation
public interface I_SAtoCA {
    List<Product> getCatalogue(String merchantId);
    OrderConfirmation placeOrder(String merchantId, List<Product> items);
    String getOrderStatus(String saOrderId);
}