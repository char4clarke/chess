package client;

import org.junit.jupiter.api.*;
import service.UserService.*;
import server.Server;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade serverFacade;
    private static String validAuthToken;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
    }

    @BeforeEach
    void clearDataBase() {
        facade.clear();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }





}
