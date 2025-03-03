package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClearServiceTests {
    private static MemoryAuthDAO authDAO;
    private static MemoryGameDAO gameDAO;
    private static MemoryUserDAO userDAO;
    private static ClearService clearService;

    @BeforeEach
    public void init() throws DataAccessException {
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        userDAO = new MemoryUserDAO();

        UserData testUser = new UserData("username", "password", "email");
        userDAO.createUser(testUser);
        String token = authDAO.createAuthToken("username");

        gameDAO.createGame("test game");

        clearService = new ClearService(userDAO, authDAO, gameDAO);
    }

    @Test
    @Order(1)
    @DisplayName("Clear User Data")
    public void clearUserData() throws DataAccessException {
        Assertions.assertNotNull(userDAO.getUser("username"));

        clearService.clear();

        Assertions.assertNull(userDAO.getUser("username"));
    }

    @Test
    @Order(2)
    @DisplayName("Clear Game Data")
    public void clearGameData() throws DataAccessException {
        Assertions.assertFalse(gameDAO.listGames().isEmpty());

        clearService.clear();

        Assertions.assertTrue(gameDAO.listGames().isEmpty());
    }

    @Test
    @Order(3)
    @DisplayName("Clear Auth Data")
    public void clearAuthData() throws DataAccessException {
        String token = authDAO.createAuthToken("username");
        AuthData auth = authDAO.getAuth(token);
        Assertions.assertNotNull(auth);

        clearService.clear();

        AuthData clearedAuth = authDAO.getAuth(token);
        Assertions.assertNull(clearedAuth);
    }

}
