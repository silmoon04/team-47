package ie.cortexx.model;

import ie.cortexx.enums.UserRole;
import java.time.LocalDateTime;

// maps to `users` table
// used by: AuthService, UserDAO, SessionManager
public class User {
    private int userId;
    private String username;
    private String passwordHash;
    private String fullName;
    private UserRole role;
    private boolean active;
    private LocalDateTime createdAt;

    // TODO: generate getters & setters (intellij: Alt+Insert)
}