package ie.cortexx.model;

import ie.cortexx.enums.UserRole;
import java.time.LocalDateTime;

// maps to `users` table
// demo users: sysdba/masterkey, manager/Get_it_done, accountant/Count_money, clerk/Paperwork
public class User {
    private int userId;
    private String username;
    private String passwordHash;
    private String fullName;
    // nullable in schema, demo users dont all have them
    private String email;
    private String phone;
    private UserRole role;
    private boolean active;
    private LocalDateTime createdAt;
    // DEFAULT 1, all users belong to cosymed
    private int merchantId;

    // TODO: generate getters & setters (intellij: Alt+Insert)
}
