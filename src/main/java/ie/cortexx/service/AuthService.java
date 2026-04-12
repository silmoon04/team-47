package ie.cortexx.service;

// handles login and logout
// hashes password with SHA-256 then checks against UserDAO
// on success, stores the user in SessionManager

// TODO: constructor taking UserDAO
// TODO: authenticate(username, password) -> boolean
// TODO: logout()
// TODO: static hashPassword(String) -> String (SHA-256)

import ie.cortexx.dao.UserDAO;
import ie.cortexx.model.User;
import ie.cortexx.util.SessionManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.HexFormat;

public class AuthService {
    private final UserDAO userDAO;

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public boolean authenticate(String username, String password) {
        try {
            String passwordHash = hashPassword(password);
            User user = userDAO.authenticate(username, passwordHash);
            if (user == null) {
                SessionManager.getInstance().logout();
                return false;
            }
            SessionManager.getInstance().login(user);
            return true;
        } catch (SQLException error) {
            SessionManager.getInstance().logout();
            return false;
        }
    }

    public void logout() {
        SessionManager.getInstance().logout();
    }

    public static String hashPassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("password is required");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("sha-256 not available", error);
        }
    }
}
