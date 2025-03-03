package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.MemoryGameDAO;
import model.GameData;
import org.junit.jupiter.api.*;
import passoff.model.*;
import passoff.server.TestServerFacade;
import server.Server;

import java.net.HttpURLConnection;
import java.util.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GameServiceTests {
    private static MemoryGameDAO gameDAO;
    private static Server server;
    private static GameService gameService;
    private static TestServerFacade serverFacade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);


        serverFacade = new TestServerFacade("localhost", Integer.toString(port));
        gameDAO = new MemoryGameDAO();
        gameService = new GameService(gameDAO);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void setup() {
        serverFacade.clear();
        try {
            gameDAO.clear();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(1)
    @DisplayName("Create Game (Positive)")
    public void createGamePositive() {
        GameService.CreateGameRequest request = new GameService.CreateGameRequest("test game");
        GameService.CreateGameResult result = GameService.createGame(request);


        Assertions.assertNotNull(result.gameID());
        Assertions.assertTrue(result.gameID() >= 0);
        Assertions.assertEquals("Success", result.message());
    }


    @Test
    @Order(2)
    @DisplayName("Create Game (Negative)")
    public void createGameNegative() {
        GameService.CreateGameRequest request = new GameService.CreateGameRequest(null);
        GameService.CreateGameResult result = GameService.createGame(request);

        Assertions.assertNull(result.gameID());
        Assertions.assertEquals("Error: Game name is empty", result.message());
    }


    @Test
    @Order(3)
    @DisplayName("List Games (Positive)")
    public void listGamesPositive() {
        GameService.createGame(new GameService.CreateGameRequest("test game 1"));
        GameService.createGame(new GameService.CreateGameRequest("test game 2"));
        GameService.createGame(new GameService.CreateGameRequest("test game 3"));
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
    @DisplayName("Get Games (Positive)")
    public void getGamePositive() {
        GameService.CreateGameResult createGameResult = GameService.createGame(new GameService.CreateGameRequest("test game"));
        GameService.GetGameResult getGameResult = gameService.getGame(new GameService.GetGameRequest(createGameResult.gameID()));

        Assertions.assertNotNull(getGameResult.game());
        Assertions.assertEquals(createGameResult.gameID(), getGameResult.game().gameID());
        Assertions.assertEquals("Success", getGameResult.message());
    }

    @Test
    @Order(6)
    @DisplayName("Get Games (Negative)")
    public void getGameNegative() {
        GameService.GetGameResult result = gameService.getGame(new GameService.GetGameRequest(-1));

        Assertions.assertNull(result.game());
        Assertions.assertTrue(result.message().contains("Error:"));
    }























    private void assertHttpOk(TestResult result) {
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, serverFacade.getStatusCode(),
                "Server response code was not 200 OK (message: %s)".formatted(result.getMessage()));
        Assertions.assertFalse(result.getMessage() != null &&
                        result.getMessage().toLowerCase(Locale.ROOT).contains("error"),
                "Result returned an error message");
    }

    private void assertHttpBadRequest(TestResult result) {
        assertHttpError(result, HttpURLConnection.HTTP_BAD_REQUEST, "Bad Request");
    }

    private void assertHttpUnauthorized(TestResult result) {
        assertHttpError(result, HttpURLConnection.HTTP_UNAUTHORIZED, "Unauthorized");
    }

    private void assertHttpForbidden(TestResult result) {
        assertHttpError(result, HttpURLConnection.HTTP_FORBIDDEN, "Forbidden");
    }

    private void assertHttpError(TestResult result, int statusCode, String message) {
        Assertions.assertEquals(statusCode, serverFacade.getStatusCode(),
                "Server response code was not %d %s (message: %s)".formatted(statusCode, message, result.getMessage()));
        Assertions.assertNotNull(result.getMessage(), "Invalid Request didn't return an error message");
        Assertions.assertTrue(result.getMessage().toLowerCase(Locale.ROOT).contains("error"),
                "Error message didn't contain the word \"Error\"");
    }

    private void assertAuthFieldsMissing(TestAuthResult result) {
        Assertions.assertNull(result.getUsername(), "Response incorrectly returned username");
        Assertions.assertNull(result.getAuthToken(), "Response incorrectly return authentication String");
    }

}
