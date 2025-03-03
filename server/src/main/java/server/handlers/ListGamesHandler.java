package server.handlers;

import static spark.Spark.get;
import com.google.gson.Gson;
import service.GameService;

public class ListGamesHandler {

    private final Gson serializer = new Gson();

    private record Message(String message) {}

    public ListGamesHandler(GameService gameService) {
        get("/game", (req, res) -> {
            String authToken = req.headers("authorization");
            if (authToken == null) {
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
