package ie.cortexx.dao;

import ie.cortexx.model.User;
import java.sql.*;
import java.util.List;

// handles all SQL for the `users` table
// owner: Alex (week 2)
public class UserDAO {

    // TODO: authenticate user - SELECT where username & password_hash match
    public User authenticate(String username, String passwordHash) throws SQLException {
        return null;
    }

    // TODO: find user by id
    public User findById(int userId) throws SQLException {
        return null;
    }

    // TODO: get all users
    public List<User> findAll() throws SQLException {
        return null;
    }

    // TODO: insert new user
    public void save(User user) throws SQLException {
    }

    // TODO: update existing user
    public void update(User user) throws SQLException {
    }
}