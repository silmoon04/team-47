package ie.cortexx.service;

// handles login and logout
// hashes password with SHA-256 then checks against UserDAO
// on success, stores the user in SessionManager

// TODO: constructor taking UserDAO
// TODO: authenticate(username, password) -> boolean
// TODO: logout()
// TODO: static hashPassword(String) -> String (SHA-256)

public class AuthService {
    // placeholder variables
    private String username;
    private String password;

    // placeholder constructor
    public AuthService() {

    }

    // placeholder method
    public boolean authenticate(String username, String password) {
        this.username = username;
        this.password = password;
        return true;
    }

    // placeholder method
    public static void hashPassword(String password) {

    }
}
