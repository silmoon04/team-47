package ie.cortexx.exception;

// thrown when a sale tries to sell more than we have in stock
public class InsufficientStockException extends RuntimeException {
    // TODO: add fields like productId, requested, available if needed
    public InsufficientStockException(String message) {
        super(message);
    }
}