package server.handlers;

import com.google.gson.Gson;
import service.GameService;

import static spark.Spark.post;

public class CreateGameHandler {
    private final Gson serializer = new Gson();

    private record Message(String message) {}

    public CreateGameHandler(GameService gameService) {


        post("/game", (req, res) -> {
            String authToken = req.headers("authorization");
            if (authToken == null) {
                res.status(401);
                res.type("application/json");
                return serializer.toJson(new Message("Error: unauthorized"));
            }

            String request = req.body();
            if (request == null) {
                res.status(400);
                res.type("application/json");
                return serializer.toJson(new Message("Error: bad request"));
            }
            try {
                GameService.CreateGameRequest createGameRequest = serializer.fromJson(req.body(), GameService.CreateGameRequest.class);
                if (createGameRequest.gameName() == null) {
                    res.status(400);
                    res.type("application/json");
                    return serializer.toJson(new Message("Error: bad request"));
                }

                GameService.CreateGameResult createGameResult = GameService.createGame(createGameRequest);
                res.status(200);
                res.type("application/json");
                return serializer.toJson(createGameRequest);
            }
            catch (Exception e) {
                res.status(500);
                res.type("application/json");
                return serializer.toJson(new Message("Error: " + e.getMessage()));
            }
        });
    }
}
