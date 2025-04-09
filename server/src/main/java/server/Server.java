package server;

import dataaccess.*;
import server.handlers.*;
import server.websocket.WebSocketHandler;
import service.ClearService;
import service.GameService;
import service.UserService;
import spark.*;

public class Server {

    public int run(int desiredPort) {

        MySqlUserDAO userDAO = new MySqlUserDAO();
        MySqlAuthDAO authDAO = new MySqlAuthDAO();
        MySqlGameDAO gameDAO = new MySqlGameDAO();

        UserService userService = new UserService(userDAO, authDAO);
        GameService gameService = new GameService(gameDAO, authDAO);
        ClearService clearService = new ClearService(userDAO, authDAO, gameDAO);
        WebSocketHandler.setServices(gameService, userService);

        Spark.webSocket("/ws", WebSocketHandler.class);
        Spark.staticFiles.location("web");
        Spark.port(desiredPort);

        new RegisterHandler(userService);
        new LoginHandler(userService);
        new LogoutHandler(userService);
        new CreateGameHandler(gameService);
        new ListGamesHandler(gameService, userService);
        new JoinGameHandler(gameService, userService);
        new ClearHandler(clearService);

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
