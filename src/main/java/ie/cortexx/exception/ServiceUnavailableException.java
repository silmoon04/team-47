package ie.cortexx.exception;

// thrown when a subsystem cant be reached (SA down, db down, etc)
// used by I_SAtoCA and I_CAtoPU
public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}
