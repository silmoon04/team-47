package ie.cortexx.service;

import ie.cortexx.dao.UserDAO;
import ie.cortexx.enums.UserRole;
import ie.cortexx.model.User;
import ie.cortexx.util.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserDAO userDAO;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        SessionManager.getInstance().logout();
        authService = new AuthService(userDAO);
    }

    @Test
    void authenticate_logs_user_into_session() throws Exception {
        User user = new User("sysdba", "masterkey", "System Admin", UserRole.ADMIN);
        when(userDAO.authenticate("sysdba", "masterkey")).thenReturn(user);

        assertTrue(authService.authenticate("sysdba", "masterkey"));
        assertSame(user, SessionManager.getInstance().getCurrentUser());
    }

    @Test
    void authenticate_rejects_bad_credentials() throws Exception {
        when(userDAO.authenticate("bad", "pw")).thenReturn(null);

        assertFalse(authService.authenticate("bad", "pw"));
        assertFalse(SessionManager.getInstance().isLoggedIn());
    }

    @Test
    void logout_clears_session() {
        SessionManager.getInstance().login(new User("manager", "pw", "Manager", UserRole.MANAGER));

        authService.logout();

        assertFalse(SessionManager.getInstance().isLoggedIn());
    }

    @Test
    void hash_password_is_stable() {
        assertEquals("demo", AuthService.hashPassword("demo"));
    }
}