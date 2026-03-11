package ie.cortexx.exception;

// thrown when a credit sale would exceed the customer's credit limit
public class InsufficientCreditException extends RuntimeException {
    public InsufficientCreditException(String message) {
        super(message);
    }
}