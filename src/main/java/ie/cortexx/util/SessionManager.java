package ie.cortexx.util;

import ie.cortexx.model.User;

// tracks who is logged in right now - singleton
// MainFrame uses this to decide which tabs to show
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    // TODO: implement getInstance() - lazy singleton, synchronized
    public static SessionManager getInstance() {
        return null;
    }

    // TODO: login(User), logout(), getCurrentUser(), isLoggedIn()
    // TODO: getCurrentRole(), hasRole(UserRole) — needs User.getRole() first
}