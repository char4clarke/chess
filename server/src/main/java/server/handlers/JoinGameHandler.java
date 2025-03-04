package server.handlers;

import static spark.Spark.put;
import com.google.gson.Gson;
import service.GameService;
import service.UserService;

// class to handle the http requests for join game
public class JoinGameHandler {

    private final Gson serializer = new Gson();

    private record Message(String message) {}

    public record JoinGameRequest(String playerColor, Integer gameID) {}

    public JoinGameHandler(GameService gameService) {
        put("/game", (req, res) -> {
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
                JoinGameRequest joinGameRequest = serializer.fromJson(request, JoinGameRequest.class);

                if (joinGameRequest.gameID == null) {
                    res.status(400);
                    res.type("application/json");
                    return serializer.toJson(new Message("Error: bad request"));
                } else if (joinGameRequest.playerColor == null
                        || (!joinGameRequest.playerColor().equals("WHITE")
                        && !joinGameRequest.playerColor().equals("BLACK"))) {
                    res.status(400);
                    res.type("application/json");
                    return serializer.toJson(new Message("Error: unauthorized"));
                }

                try {
                    UserService.validateAuthToken(authToken);
                }
                catch (Exception e) {
                    res.status(401);
                    res.type("application/json");
                    return serializer.toJson(new Message("Error: unauthorized"));
                }

                GameService.JoinGameRequest serviceJoinRequest = new GameService.JoinGameRequest(joinGameRequest.playerColor, joinGameRequest.gameID);
                GameService.JoinGameResult joinGameResult = gameService.joinGame(serviceJoinRequest, authToken);

                if (joinGameResult.message().contains("taken")) {
                    res.status(403);
                    res.type("application/json");
                    return serializer.toJson(new Message("Error: already taken"));
                } else if (joinGameResult.message().contains("Success")) {
                    res.status(200);
                    res.type("application/json");
                    return "{}";
                } else {
                    res.status(400);
                    res.type("application/json");
                    return serializer.toJson(new Message(joinGameResult.message()));
                }

            }
            catch (Exception e) {
                res.status(500);
                res.type("application/json");
                return serializer.toJson(new Message("Error: " + e.getMessage()));
            }
        });
    }

}
