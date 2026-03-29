package ie.cortexx.util;

import ie.cortexx.model.User;
import ie.cortexx.enums.UserRole;

// tracks who is logged in right now, singleton (static)
// MainFrame uses this to decide which tabs to show
//
// basically:
//   1. getInstance() returns the single instance (lazy init, synchronized)
//   2. login(user) stores the user after successful auth
//   3. getCurrentRole() returns the enum so MainFrame can switch tabs
//   4. logout() clears everything, MainFrame goes back to LoginPanel

public class SessionManager {
    private static SessionManager instance;
    private volatile User currentUser; //volatile is good for caching related problems

    private SessionManager() {}

    // lazy singleton, synchronized so its thread safe
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // called by AuthService after successful authentication
    public void login(User user) {
        this.currentUser = user;
    }

    // called on logout, clears session
    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    // returns the role enum of the logged in user
    // MainFrame uses this to decide which tabs to show
    // returns null if nobody is logged in
    public UserRole getCurrentRole() {
        if (currentUser == null) return null;
        return currentUser.getRole();
    }

    // convenience check, e.g. hasRole(UserRole.MANAGER)
    public boolean hasRole(UserRole role) {
        if (currentUser == null) return false;
        return currentUser.getRole() == role;
    }
}
