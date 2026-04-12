package ie.cortexx.util;

// simple input validation helpers
// use these when taking input from the GUI before passing to services
public class ValidationUtils {
    private ValidationUtils() {}

    public static boolean isNullOrBlank(String value) {
        return value == null || value.isBlank();
    }

    public static String requireNonBlank(String value, String fieldName) {
        if (isNullOrBlank(value)) throw new IllegalArgumentException(fieldName + " is required");
        return value.trim();
    }

    public static int requirePositive(int value, String fieldName) {
        if (value <= 0) throw new IllegalArgumentException(fieldName + " must be greater than 0");
        return value;
    }
}