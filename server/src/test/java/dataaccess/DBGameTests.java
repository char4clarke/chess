package dataaccess;


import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;

import java.util.List;


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


    @Test
    @Order(5)
    @DisplayName("Join Game (Positive)")
    public void JoinGamePositive() throws DataAccessException {
        int gameID = gameDAO.createGame("test game");
        gameDAO.joinGame(gameID, "username", "WHITE");
        GameData game = gameDAO.getGame(gameID);
        Assertions.assertEquals("username", game.whiteUsername());
    }

    @Test
    @Order(6)
    @DisplayName("Join Game (Negative)")
    public void JoinGameNegative() throws DataAccessException {
        try {
            int gameID = gameDAO.createGame("test game");
            gameDAO.joinGame(gameID, "user1", "WHITE");
            gameDAO.joinGame(gameID, "user2", "WHITE");
        } catch (DataAccessException e) {
            Assertions.assertTrue(e.getMessage().contains("Error:"));
        }
    }

    @Test
    @Order(7)
    @DisplayName("List Games (Positive)")
    public void listGamesPositive() throws DataAccessException {
        gameDAO.createGame("test game 1");
        gameDAO.createGame("test game 2");
        gameDAO.createGame("test game 3");
        List<GameData> games = gameDAO.listGames();
        Assertions.assertEquals(3, games.size());
    }

    @Test
    @Order(8)
    @DisplayName("List Games (Negative)")
    public void listGamesNegative() throws DataAccessException {
        List<GameData> games = gameDAO.listGames();
        Assertions.assertEquals(0, games.size());
    }

    @Test
    @Order(9)
    @DisplayName("Clear Game Data")
    public void clearGameData() throws DataAccessException {
        gameDAO.createGame("test game 1");
        gameDAO.createGame("test game 2");
        gameDAO.createGame("test game 3");
        Assertions.assertEquals(3, gameDAO.listGames().size());

        gameDAO.clear();
        Assertions.assertEquals(0, gameDAO.listGames().size());
    }

}
