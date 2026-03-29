package ie.cortexx.exception;

// thrown when an order id doesnt exist in SA
// used by I_SAtoCA.getOrderStatus()
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}
