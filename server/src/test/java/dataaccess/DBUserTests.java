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
}
