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

    // find user by id
    public User findById(int userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (var c = DBConnection.getConnection();
            var ps = c.prepareStatement(sql)){
            ps.setInt(1, userId);
            try (var rs = ps.executeQuery()){
                if (!rs.next()) return null;
                return mapUser(rs);
            }
        }
    }

    // get all users
    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM users";
        List<User> users = new ArrayList<>();

        try (var c = DBConnection.getConnection();
            var ps = c.prepareStatement(sql);
            var rs = ps.executeQuery()){
            while (rs.next()){
                users.add(mapUser(rs));
            }
        }
        return users;
    }

    // insert new user
    public void save(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, full_name, email, phone, role, is_active, merchant_id) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (var c = DBConnection.getConnection();
            var ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getRole().name());
            ps.setBoolean(7, user.isActive());
            ps.setInt(8, user.getMerchantId());
            ps.executeUpdate();

            try (var rs = ps.getGeneratedKeys()){
                if (rs.next()){
                    user.setUserId(rs.getInt(1));
                }
            }
        }
    }

    // update existing user
    public void update(User user) throws SQLException {
        String sql = "UPDATE users SET username = ?, password_hash = ?, full_name = ?, email = ?, phone = ?, " + "role = ?, is_active = ?, merchant_id = ? WHERE user_id = ?";

        try (var c = DBConnection.getConnection();
            var ps = c.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getRole().name());
            ps.setBoolean(7, user.isActive());
            ps.setInt(8, user.getMerchantId());
            ps.setInt(9, user.getUserId());

            ps.executeUpdate();
        }
    }

    // soft delete
    public void deactivate(int userId) throws SQLException {
        String sql = "UPDATE users SET is_active = FALSE WHERE user_id = ?";

        try (var c = DBConnection.getConnection();
            var ps = c.prepareStatement(sql)){
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
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
