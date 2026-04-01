package ie.cortexx.gui;

/**
 * Immutable session snapshot for the currently signed-in GUI user.
 */
public record UserSession(String username, String role) {
    public UserSession {
        username = username == null ? "" : username.trim();
        role = role == null ? "" : role.trim();
    }

    public static UserSession guest() {
        return new UserSession("", "");
    }

    public static UserSession anonymous() {
        return guest();
    }

    public UserSession withRole(String nextRole) {
        return new UserSession(username, nextRole);
    }
}
