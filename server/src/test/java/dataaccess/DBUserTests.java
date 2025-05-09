package dataaccess;


import model.UserData;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DBUserTests {
    private static MySqlUserDAO userDAO;

    @BeforeEach
    public void setup() throws DataAccessException {
        userDAO = new MySqlUserDAO();
        userDAO.clear();
    }

    @Test
    @Order(1)
    @DisplayName("Create User (Positive)")
    public void createUserPositive() throws DataAccessException {
        userDAO.createUser(new UserData("username", "password", "type"));
        UserData user = userDAO.getUser("username");
        Assertions.assertEquals("username", user.username());
    }

    @Test
    @Order(2)
    @DisplayName("Create User (Negative)")
    public void createUserNegative() {
        try {
            userDAO.createUser(new UserData("username", "password", "type"));
            userDAO.createUser(new UserData("username", "password1", "type1"));
        } catch (DataAccessException e) {
            Assertions.assertTrue(e.getMessage().contains("Error:"));
        }
    }

    @Test
    @Order(3)
    @DisplayName("Get User (Positive)")
    public void getUserPositive() throws DataAccessException {
        UserData user = new UserData("username", "password", "type");
        userDAO.createUser(user);
        UserData getUser = userDAO.getUser("username");
        Assertions.assertNotNull(getUser);
        Assertions.assertEquals("username", getUser.username());
    }

    @Test
    @Order(4)
    @DisplayName("Get User (Negative)")
    public void getUserNegative() throws DataAccessException {
        UserData getUser = userDAO.getUser("username");
        Assertions.assertNull(getUser);
    }

    @Test
    @Order(5)
    @DisplayName("Validate User (Positive)")
    public void validateUserPositive() throws DataAccessException {
        UserData user = new UserData("username", "password", "type");
        userDAO.createUser(user);
        boolean isValid = userDAO.validateUser("username", "password");
        Assertions.assertTrue(isValid);
    }

    @Test
    @Order(6)
    @DisplayName("Validate User (Negative)")
    public void validateUserNegative() throws DataAccessException {
        UserData user = new UserData("username", "password", "type");
        userDAO.createUser(user);
        boolean isValid = userDAO.validateUser("username", "wrong");
        Assertions.assertFalse(isValid);
    }

    @Test
    @Order(7)
    @DisplayName("Clear User Data")
    public void clearUserData() throws DataAccessException {
        userDAO.createUser(new UserData("username1", "password1", "type1"));
        userDAO.createUser(new UserData("username2", "password2", "type2"));
        userDAO.createUser(new UserData("username3", "password3", "type3"));

        userDAO.clear();
        Assertions.assertNull(userDAO.getUser("username1"));
        Assertions.assertNull(userDAO.getUser("username2"));
        Assertions.assertNull(userDAO.getUser("username3"));
    }
}
