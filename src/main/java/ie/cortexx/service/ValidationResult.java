package ie.cortexx.service;

public class ValidationResult {
    private boolean valid;
    private String message;

    public ValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public static ValidationResult ok() {
        return new ValidationResult(true, null);
    }
    public static ValidationResult fail(String message) {
        return new ValidationResult(false, message);
    }

    public boolean isValid() {
        return valid;
    }
    public String getMessage() {
        return message;
    }
}
