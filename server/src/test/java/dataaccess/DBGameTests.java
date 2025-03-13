package dataaccess;


import model.GameData;
import org.junit.jupiter.api.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DBGameTests {
    private static MySqlGameDAO gameDAO;

    @BeforeEach
    public void setup() throws DataAccessException {
        gameDAO = new MySqlGameDAO();
        gameDAO.clear();
    }

    @Test
    @Order(1)
    @DisplayName("Create Game (Positive)")
    public void createGamePositive() throws DataAccessException {
        int gameID = gameDAO.createGame("test game");
        Assertions.assertTrue(gameID > 0);
    }

    @Test
    @Order(2)
    @DisplayName("Create Game (Negative)")
    public void createGameNegative() {
        try {
            gameDAO.createGame(null);
            Assertions.fail("Error not thrown");
        } catch (DataAccessException e) {
            Assertions.assertTrue(e.getMessage().contains("Error:"));
        }
    }

    @Test
    @Order(3)
    @DisplayName("Get Game (Positive)")
    public void getGamePositive() throws DataAccessException {
        int gameID = gameDAO.createGame("test game");
        GameData game = gameDAO.getGame(gameID);
        Assertions.assertNotNull(game);
        Assertions.assertEquals(gameID, game.gameID());
    }

    @Test
    @Order(4)
    @DisplayName("Get Game (Negative)")
    public void getGameNegative() throws DataAccessException {
        GameData game = gameDAO.getGame(-1);
        Assertions.assertNull(game);
    }




}
