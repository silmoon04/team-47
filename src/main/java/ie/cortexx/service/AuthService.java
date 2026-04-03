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

import java.sql.SQLException;

public class AuthService {
    // placeholder variables
    private String username;
    private String password;
    private final UserDAO userDAO;
    // placeholder constructor
    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;

    }

    // placeholder method
    public boolean authenticate(String username, String password) {
        try{
            String passwordHash = hashPassword(password);
            User user = userDAO.authenticate(username, passwordHash);
            if (user == null) {
                return false;
            }
            SessionManager.getInstance().login(user);
            return true;
        } catch (SQLException error){
            System.out.println(error.getMessage());
            return false;
        }

    }



    // placeholder method
    public static String hashPassword(String password) {
        return password;

    }
}
