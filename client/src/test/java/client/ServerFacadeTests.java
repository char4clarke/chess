package client;

import exception.ResponseException;
import org.junit.jupiter.api.*;
import server.Server;
import ui.ServerFacade;
import ui.ServerFacade.*;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade serverFacade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        serverFacade = new ServerFacade("http://localhost:" + port);
    }

    @BeforeEach
    void clearDataBase() throws ResponseException {
        serverFacade.clear();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void registerPositive() throws Exception {
        RegisterRequest request = new RegisterRequest("player1", "password", "p1@email.com");
        RegisterResult result = serverFacade.register(request);
        Assertions.assertNotNull(result.authToken());
        Assertions.assertTrue(result.authToken().length() > 10);
    }

    @Test
    public void registerNegative() throws Exception {
        RegisterRequest request = new RegisterRequest("player1", null, "p1@email.com");
        ResponseException e = Assertions.assertThrows(ResponseException.class, () -> serverFacade.register(request));
        Assertions.assertTrue(e.getMessage().contains("Error"));
    }


    @Test
    public void loginPositive() throws Exception {
        RegisterRequest regRequest = new RegisterRequest("player1", "password", "p1@email.com");
        serverFacade.register(regRequest);
        LoginRequest loginRequest = new LoginRequest("player1", "password");
        LoginResult result = serverFacade.login(loginRequest);
        Assertions.assertNotNull(result.authToken());
    }

    @Test
    public void loginNegative() throws Exception {
        RegisterRequest regRequest = new RegisterRequest("player1", "password", "p1@email.com");
        serverFacade.register(regRequest);
        LoginRequest loginRequest = new LoginRequest("player1", "incorrect");
        ResponseException e = Assertions.assertThrows(ResponseException.class, () -> serverFacade.login(loginRequest));
        Assertions.assertTrue(e.getMessage().contains("Error"));
    }


    @Test
    public void logoutPositive() throws Exception {
        RegisterRequest regRequest = new RegisterRequest("player1", "password", "p1@email.com");
        RegisterResult regResult = serverFacade.register(regRequest);
        LogoutRequest request = new LogoutRequest(regResult.authToken());
        Assertions.assertDoesNotThrow(() -> serverFacade.logout(request));
    }

    @Test
    public void logoutNegative() throws Exception {
        RegisterRequest regRequest = new RegisterRequest("player1", "password", "p1@email.com");
        serverFacade.register(regRequest);
        LogoutRequest request = new LogoutRequest("incorrect");
        ResponseException e = Assertions.assertThrows(ResponseException.class, () -> serverFacade.logout(request));
        Assertions.assertEquals(401, e.statusCode());
    }


    @Test
    public void createGamePositive() throws Exception {
        RegisterRequest regRequest = new RegisterRequest("player1", "password", "p1@email.com");
        RegisterResult regResult = serverFacade.register(regRequest);
        CreateGameRequest request = new CreateGameRequest("test_game");
        CreateGameResult result = serverFacade.createGame(request, regResult.authToken());
        Assertions.assertNotNull(result.gameID());
    }

    @Test
    public void createGameNegative() throws Exception {
        RegisterRequest regRequest = new RegisterRequest("player1", "password", "p1@email.com");
        serverFacade.register(regRequest);
        CreateGameRequest request = new CreateGameRequest("test_game");
        ResponseException e = Assertions.assertThrows(ResponseException.class, () -> serverFacade.createGame(request, "incorrect"));
        Assertions.assertEquals(401, e.statusCode());
    }

    @Test
    public void listGamesPositive() throws Exception {
        RegisterRequest regRequest = new RegisterRequest("player1", "password", "p1@email.com");
        RegisterResult regResult = serverFacade.register(regRequest);
        CreateGameRequest request = new CreateGameRequest("test_game");
        serverFacade.createGame(request, regResult.authToken());

        ListGamesResult result = serverFacade.listGames(regResult.authToken());
        Assertions.assertNotEquals(0, result.games().size());
    }

    @Test
    public void listGamesNegative() throws Exception {
        RegisterRequest regRequest = new RegisterRequest("player1", "password", "p1@email.com");
        RegisterResult regResult = serverFacade.register(regRequest);
        CreateGameRequest request = new CreateGameRequest("test_game");
        serverFacade.createGame(request, regResult.authToken());

        ResponseException e = Assertions.assertThrows(ResponseException.class, () -> serverFacade.listGames("incorrect"));
        Assertions.assertEquals(401, e.statusCode());
    }


    @Test
    public void joinGamePositive() throws Exception {
        RegisterRequest regRequest = new RegisterRequest("player1", "password", "p1@email.com");
        RegisterResult regResult = serverFacade.register(regRequest);
        CreateGameRequest createRequest = new CreateGameRequest("test_game");
        CreateGameResult createResult = serverFacade.createGame(createRequest, regResult.authToken());

        JoinGameRequest request = new JoinGameRequest("WHITE", createResult.gameID());
        Assertions.assertDoesNotThrow(() -> serverFacade.joinGame(request, regResult.authToken()));
    }

    @Test
    public void joinGameNegative() throws Exception {
        RegisterRequest regRequest = new RegisterRequest("player1", "password", "p1@email.com");
        RegisterResult regResult = serverFacade.register(regRequest);

        JoinGameRequest request = new JoinGameRequest("WHITE", 45);
        ResponseException e = Assertions.assertThrows(ResponseException.class, () -> serverFacade.joinGame(request, regResult.authToken()));
        Assertions.assertEquals(400, e.statusCode());
    }


    @Test
    public void clearTest() throws Exception {
        RegisterRequest regRequest = new RegisterRequest("player1", "password", "p1@email.com");
        RegisterResult regResult = serverFacade.register(regRequest);
        CreateGameRequest createRequest = new CreateGameRequest("test_game");
        serverFacade.createGame(createRequest, regResult.authToken());
        Assertions.assertEquals(1, serverFacade.listGames(regResult.authToken()).games().size());

        Assertions.assertDoesNotThrow(() -> serverFacade.clear());

        RegisterResult newRegResult = serverFacade.register(regRequest);
        ListGamesResult listResult = serverFacade.listGames(newRegResult.authToken());
        Assertions.assertEquals(0, listResult.games().size());
    }
}
