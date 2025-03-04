package server.handlers;

import com.google.gson.Gson;
import service.GameService;

import static spark.Spark.post;

// class to handle the http requests for create game
public class CreateGameHandler {
    private final Gson serializer = new Gson();

    private record Message(String message) {}

    public CreateGameHandler(GameService gameService) {


        post("/game", (req, res) -> {
            String authToken = req.headers("authorization");
            GameService.CreateGameRequest request = serializer.fromJson(req.body(), GameService.CreateGameRequest.class);
            GameService.CreateGameResult result = gameService.createGame(request, authToken);

            if (authToken == null) {
                res.status(401);
                res.type("application/json");
                return serializer.toJson(new Message("Error: unauthorized"));
            }

            if (result.message().equals("Error: unauthorized")) {
                res.status(401);
            } else if (result.message().contains("Error: bad request")) {
                res.status(400);
            } else if (result.message().contains("Error:")) {
                res.status(500);
            } else {
                res.status(200);
            }

            res.type("application/json");
            return serializer.toJson(result);

        });
    }
}
