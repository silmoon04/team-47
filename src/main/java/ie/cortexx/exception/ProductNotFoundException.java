package ie.cortexx.exception;

// thrown when a product id doesnt exist in our db
// used by I_CAtoPU when PU asks for a product we dont have
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String message) {
        super(message);
    }
}
