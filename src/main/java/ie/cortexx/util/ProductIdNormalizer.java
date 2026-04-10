package ie.cortexx.util;

public final class ProductIdNormalizer {
    private ProductIdNormalizer() {
    }

    public static String normalize(String value) {
        return value == null ? "" : value.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    public static boolean matches(String left, String right) {
        return !normalize(left).isEmpty() && normalize(left).equals(normalize(right));
    }
}