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




}
