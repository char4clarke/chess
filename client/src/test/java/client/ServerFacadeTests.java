package client;

import exception.ResponseException;
import org.junit.jupiter.api.*;
import service.UserService.*;
import server.Server;
import ui.ServerFacade;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade serverFacade;
    private static String validAuthToken;

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
        Assertions.assertEquals(401, e.StatusCode());
    }

}
