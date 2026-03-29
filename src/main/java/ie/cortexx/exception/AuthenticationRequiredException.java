package ie.cortexx.exception;

// thrown when SA requires auth but merchant session is invalid/expired
// used by I_SAtoCA methods that need an active session
public class AuthenticationRequiredException extends RuntimeException {
    public AuthenticationRequiredException(String message) {
        super(message);
    }
}
