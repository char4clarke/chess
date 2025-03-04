package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import org.junit.jupiter.api.*;



@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GameServiceTests {
    private static MemoryAuthDAO authDAO;
    private static GameService gameService;

    @BeforeEach
    public void init() {
        MemoryGameDAO gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        gameService = new GameService(gameDAO, authDAO);

        try {
            gameDAO.clear();
            authDAO.clear();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(1)
    @DisplayName("Create Game (Positive)")
    public void createGamePositive() throws DataAccessException {
        String token = authDAO.createAuthToken("username");
        GameService.CreateGameRequest request = new GameService.CreateGameRequest("test game");
        GameService.CreateGameResult result = gameService.createGame(request, token);

        Assertions.assertNotNull(result.gameID());
        Assertions.assertTrue(result.gameID() >= 0);
        Assertions.assertEquals("Success", result.message());
    }


    @Test
    @Order(2)
    @DisplayName("Create Game (Negative)")
    public void createGameNegative() throws DataAccessException {
        String token = authDAO.createAuthToken("username");
        GameService.CreateGameRequest request = new GameService.CreateGameRequest(null);
        GameService.CreateGameResult result = gameService.createGame(request, token);

        // assert no game name given
        Assertions.assertNull(result.gameID());
        Assertions.assertEquals("Error: no game name", result.message());
    }


    @Test
    @Order(3)
    @DisplayName("List Games (Positive)")
    public void listGamesPositive() throws DataAccessException {
        String token1 = authDAO.createAuthToken("username1");
        String token2 = authDAO.createAuthToken("username2");
        String token3 = authDAO.createAuthToken("username3");

        gameService.createGame(new GameService.CreateGameRequest("test game 1"), token1);
        gameService.createGame(new GameService.CreateGameRequest("test game 2"), token2);
        gameService.createGame(new GameService.CreateGameRequest("test game 3"), token3);

        GameService.ListGamesResult result = gameService.listGames();

        Assertions.assertNotNull(result.games());
        Assertions.assertEquals(3, result.games().size());
        Assertions.assertEquals("Success", result.message());
    }

    @Test
    @Order(4)
    @DisplayName("List Games (Negative)")
    public void listGamesNegative() {
        GameService.ListGamesResult result = gameService.listGames();

        // assert no games in list
        Assertions.assertEquals(0, result.games().size());
        Assertions.assertNotNull(result);
    }

    @Test
    @Order(5)
    @DisplayName("Get Game (Positive)")
    public void getGamePositive() throws DataAccessException {
        String token = authDAO.createAuthToken("username");
        GameService.CreateGameResult createResult = gameService.createGame(new GameService.CreateGameRequest("test game"), token);
        GameService.GetGameResult getResult = gameService.getGame(new GameService.GetGameRequest(createResult.gameID()));

        Assertions.assertNotNull(getResult.game());
        Assertions.assertEquals(createResult.gameID(), getResult.game().gameID());
        Assertions.assertEquals("Success", getResult.message());
    }




    @Test
    @Order(6)
    @DisplayName("Get Games (Negative)")
    public void getGameNegative() {
        GameService.GetGameResult result = gameService.getGame(new GameService.GetGameRequest(-1));

        // assert invalid gameID
        Assertions.assertNull(result.game());
        Assertions.assertTrue(result.message().contains("Error:"));
    }



    @Test
    @Order(7)
    @DisplayName("Join Game (Positive)")
    public void joinGamePositive() throws DataAccessException {
        String token = authDAO.createAuthToken("username");
        GameService.CreateGameRequest createGameRequest = new GameService.CreateGameRequest("test game");
        GameService.CreateGameResult createGameResult = gameService.createGame(createGameRequest, token);

        GameService.JoinGameResult result = gameService.joinGame(new GameService.JoinGameRequest("WHITE", createGameResult.gameID()), token);

        Assertions.assertNotNull(createGameResult.gameID());
        Assertions.assertEquals("Success", result.message());
    }


    @Test
    @Order(8)
    @DisplayName("Join Game (Negative)")
    public void joinGameNegative() throws DataAccessException {
        String token = authDAO.createAuthToken("username");
        GameService.CreateGameRequest createGameRequest = new GameService.CreateGameRequest("test game");
        GameService.CreateGameResult createGameResult = gameService.createGame(createGameRequest, token);
        String invalidToken = "invalid";

        // assert invalid token
        GameService.JoinGameResult result = gameService.joinGame(new GameService.JoinGameRequest("WHITE", createGameResult.gameID()), invalidToken);
        Assertions.assertTrue(result.message().contains("Error:"));
    }

}
