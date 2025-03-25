package server.handlers;

import static spark.Spark.get;
import com.google.gson.Gson;
import service.GameService;
import service.UserService;

// class to handle the http requests for list games
public class ListGamesHandler {

    private final Gson serializer = new Gson();
    private final UserService userService;
    private final GameService gameService;

    private record Message(String message) {}

    public ListGamesHandler(GameService gameService, UserService userService) {
        this.userService = userService;
        this.gameService = gameService;
        get("/game", (req, res) -> {
            String authToken = req.headers("authorization");
            if (authToken == null) {
                res.status(401);
                res.type("application/json");
                return serializer.toJson(new Message("Error: unauthorized"));
            }
            try {
                userService.validateAuthToken(authToken);
            }
            catch (Exception e) {
                res.status(401);
                res.type("application/json");
                return serializer.toJson(new Message("Error: unauthorized"));
            }

            try {
                GameService.ListGamesResult listGamesResult = gameService.listGames();
                res.status(200);
                res.type("application/json");
                return serializer.toJson(listGamesResult);
            }
            catch (Exception e) {
                res.status(500);
                res.type("application/json");
                return serializer.toJson(new Message("Error: " + e.getMessage()));
            }
        });
    }

}
