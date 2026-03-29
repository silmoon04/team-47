package ie.cortexx.dao;

import ie.cortexx.model.User;
import ie.cortexx.enums.UserRole;
import ie.cortexx.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// handles all SQL for the `users` table
// see context/JDBC_AND_DAO_GUIDE.md for patterns
// owner: Alex
public class UserDAO {

    // -- example method (silmoon) --
    // authenticate: find user by username + password, only if active
    // returns null if no match (wrong creds or deactivated user)
    // tested in: DAOExampleIT.authenticateValidUser + authenticateWrongPassword
    public User authenticate(String username, String passwordHash) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ? AND is_active = TRUE";
        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return mapUser(rs);
            }
        }
    }

    // TODO: find user by id (same pattern as authenticate but WHERE user_id = ?)
    public User findById(int userId) throws SQLException {
        return null;
    }

    // TODO: get all users (use while(rs.next()) loop, add each to list)
    public List<User> findAll() throws SQLException {
        return null;
    }

    // TODO: insert new user (use Statement.RETURN_GENERATED_KEYS to get user_id back)
    public void save(User user) throws SQLException {
    }

    // TODO: update existing user (UPDATE ... SET ... WHERE user_id = ?)
    public void update(User user) throws SQLException {
    }

    // TODO: soft delete (UPDATE is_active = FALSE, dont actually DELETE)
    public void deactivate(int userId) throws SQLException {
    }

    // converts a ResultSet row to a User object
    // reuse this in every method that returns a User
    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setRole(UserRole.valueOf(rs.getString("role")));
        u.setActive(rs.getBoolean("is_active"));
        u.setMerchantId(rs.getInt("merchant_id"));
        return u;
    }
}