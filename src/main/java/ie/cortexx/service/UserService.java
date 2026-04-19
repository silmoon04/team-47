package ie.cortexx.service;

import ie.cortexx.dao.UserDAO;
import ie.cortexx.model.User;
import java.sql.SQLException;
import java.util.List;

public class UserService {

    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public List<User> findAll() {
        try { return userDAO.findAll(); }
        catch (SQLException e) { throw new RuntimeException(e); }
    }

    public User findById(int userId) {
        try { return userDAO.findById(userId); }
        catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void save(User user) {
        try { userDAO.save(user); }
        catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void update(User user) {
        try { userDAO.update(user); }
        catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void delete(int userId) {
        try {
            userDAO.delete(userId);
        } catch (SQLException deleteError) {
            try {
                userDAO.deactivate(userId);
            } catch (SQLException deactivateError) {
                deleteError.addSuppressed(deactivateError);
                throw new RuntimeException(deleteError);
            }
        }
    }
}
