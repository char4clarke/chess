package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import org.junit.jupiter.api.*;



@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GameServiceTests {
    private static MemoryAuthDAO authDAO;
    private static MemoryGameDAO gameDAO;
    private static GameService gameService;

    @BeforeEach
    public void setup() {
        gameDAO = new MemoryGameDAO();
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
    public void createGamePositive() {
        GameService.CreateGameRequest request = new GameService.CreateGameRequest("test game");
        GameService.CreateGameResult result = gameService.createGame(request);


        Assertions.assertNotNull(result.gameID());
        Assertions.assertTrue(result.gameID() >= 0);
        Assertions.assertEquals("Success", result.message());
    }


    @Test
    @Order(2)
    @DisplayName("Create Game (Negative)")
    public void createGameNegative() {
        GameService.CreateGameRequest request = new GameService.CreateGameRequest(null);
        GameService.CreateGameResult result = gameService.createGame(request);

        Assertions.assertNull(result.gameID());
        Assertions.assertEquals("Error: Game name is empty", result.message());
    }


    @Test
    @Order(3)
    @DisplayName("List Games (Positive)")
    public void listGamesPositive() {
        gameService.createGame(new GameService.CreateGameRequest("test game 1"));
        gameService.createGame(new GameService.CreateGameRequest("test game 2"));
        gameService.createGame(new GameService.CreateGameRequest("test game 3"));
        GameService.ListGamesResult result = gameService.listGames();

        Assertions.assertNotNull(result.allGames());
        Assertions.assertEquals(3, result.allGames().size());
        Assertions.assertEquals("Success", result.message());
    }

    @Test
    @Order(4)
    @DisplayName("List Games (Negative)")
    public void listGamesNegative() {

        GameService.ListGamesResult result = gameService.listGames();

        Assertions.assertEquals(0, result.allGames().size());
        Assertions.assertNotNull(result);
    }

    @Test
    @Order(5)
    @DisplayName("Get Game (Positive)")
    public void getGamePositive() {
        GameService.CreateGameResult createResult = gameService.createGame(new GameService.CreateGameRequest("test game"));
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

        Assertions.assertNull(result.game());
        Assertions.assertTrue(result.message().contains("Error:"));
    }



    @Test
    @Order(7)
    @DisplayName("Join Game (Positive)")
    public void joinGamePositive() throws DataAccessException {
        GameService.CreateGameRequest createGameRequest = new GameService.CreateGameRequest("test game");
        GameService.CreateGameResult createGameResult = gameService.createGame(createGameRequest);
        String token = authDAO.createAuthToken("user");

        GameService.JoinGameResult result = gameService.joinGame(new GameService.JoinGameRequest("WHITE", createGameResult.gameID()), token);

        Assertions.assertNotNull(createGameResult.gameID());
        Assertions.assertEquals("Success", result.message());
    }


    @Test
    @Order(8)
    @DisplayName("Join Game (Negative)")
    public void joinGameNegative() {
        GameService.CreateGameRequest createGameRequest = new GameService.CreateGameRequest("test game");
        GameService.CreateGameResult createGameResult = gameService.createGame(createGameRequest);
        String invalidToken = "invalid";

        GameService.JoinGameResult result = gameService.joinGame(new GameService.JoinGameRequest("WHITE", createGameResult.gameID()), invalidToken);
        Assertions.assertTrue(result.message().contains("Error:"));
    }





























}
