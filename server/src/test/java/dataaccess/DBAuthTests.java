package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DBAuthTests {
    private static MySqlAuthDAO authDAO;

    @BeforeEach
    public void setup() throws DataAccessException {
        authDAO = new MySqlAuthDAO();
        authDAO.clear();
    }

    @Test
    @Order(1)
    @DisplayName("Create Auth Token (Positive)")
    public void createAuthTokenPositive() throws DataAccessException {
        try {
            AuthData auth = new AuthData("authToken", "username");
            authDAO.createAuth(auth);
        } catch (DataAccessException e) {
            Assertions.assertTrue(e.getMessage().contains("Error:"));
        }
    }

    @Test
    @Order(2)
    @DisplayName("Create Auth Token (Negative)")
    public void createAuthTokenNegative() throws DataAccessException {
        try {
            AuthData auth = new AuthData("authToken", "username");
            authDAO.createAuth(auth);
            authDAO.createAuth(new AuthData("authToken", "username1"));
        } catch (DataAccessException e) {
            Assertions.assertTrue(e.getMessage().contains("Error:"));
        }
    }

    @Test
    @Order(3)
    @DisplayName("Get Auth Token (Positive)")
    public void getAuthTokenPositive() throws DataAccessException {
        AuthData auth = new AuthData("authToken", "username");
        authDAO.createAuth(auth);
        AuthData data = authDAO.getAuth("authToken");
        Assertions.assertEquals("username", data.username());
        Assertions.assertEquals("authToken", data.authToken());
    }

    @Test
    @Order(4)
    @DisplayName("Get Auth Token (Negative)")
    public void getAuthTokenNegative() throws DataAccessException {
        AuthData auth = authDAO.getAuth("username");
        Assertions.assertNull(auth);
    }

    @Test
    @Order(5)
    @DisplayName("Delete Auth Token (Positive)")
    public void deleteAuthPositive() throws DataAccessException {
        AuthData auth = new AuthData("authToken", "username");
        authDAO.createAuth(auth);
        authDAO.deleteAuth("authToken");
        AuthData data = authDAO.getAuth("authToken");
        Assertions.assertNull(data);
    }

    @Test
    @Order(6)
    @DisplayName("Delete Auth Token (Negative)")
    public void deleteAuthNegative() throws DataAccessException {
        try {
            authDAO.deleteAuth("authToken");
        } catch (DataAccessException e) {
            Assertions.assertTrue(e.getMessage().contains("Error:"));
        }
    }


    @Test
    @Order(7)
    @DisplayName("Clear Auth Data")
    public void clearAuthData() throws DataAccessException {
        authDAO.createAuth(new AuthData("authToken1", "username1"));
        authDAO.createAuth(new AuthData("authToken2", "username2"));
        authDAO.createAuth(new AuthData("authToken3", "username3"));
        authDAO.clear();

        Assertions.assertNull(authDAO.getAuth("authToken1"));
        Assertions.assertNull(authDAO.getAuth("authToken2"));
        Assertions.assertNull(authDAO.getAuth("authToken3"));
    }
}
