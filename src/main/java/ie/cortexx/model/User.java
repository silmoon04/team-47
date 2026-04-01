package ie.cortexx.model;

import ie.cortexx.enums.UserRole;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

// maps to `users` table
// demo users: sysdba/masterkey, manager/Get_it_done, accountant/Count_money, clerk/Paperwork
public class User {

    private int userId;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private String phone;
    private UserRole role;
    private boolean active;
    private LocalDateTime createdAt;
    // DEFAULT 1, all users belong to cosymed
    private int merchantId;

    public User() {}

    // convenience constructor for the common fields needed to create a user
    public User(String username, String passwordHash, String fullName, UserRole role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.active = true;
        this.merchantId = 1;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getMerchantId() { return merchantId; }
    public void setMerchantId(int merchantId) { this.merchantId = merchantId; }

    // builds a User from a ResultSet row so DAOs dont need a separate mapUser()
    // usage in DAO: return User.fromRS(rs);
    public static User fromRS(ResultSet rs) throws SQLException {
        User u = new User();
        u.userId = rs.getInt("user_id");
        u.username = rs.getString("username");
        u.passwordHash = rs.getString("password_hash");
        u.fullName = rs.getString("full_name");
        u.email = rs.getString("email");
        u.phone = rs.getString("phone");
        u.role = UserRole.valueOf(rs.getString("role"));
        u.active = rs.getBoolean("is_active");
        u.merchantId = rs.getInt("merchant_id");
        return u;
    }
}
