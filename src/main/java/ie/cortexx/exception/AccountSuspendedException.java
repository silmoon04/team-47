package ie.cortexx.exception;

// thrown when trying to sell on credit to a suspended/defaulted customer
public class AccountSuspendedException extends RuntimeException {
    public AccountSuspendedException(String message) {
        super(message);
    }
}