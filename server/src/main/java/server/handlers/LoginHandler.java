package server.handlers;

import com.google.gson.Gson;
import service.UserService;

import static spark.Spark.post;

public class LoginHandler {
    private final Gson serializer = new Gson();

    private record Message(String message) {}

    public LoginHandler(UserService userService) {


        post("/session", (req, res) -> {
            String request = req.body();
            if (request == null) {
                res.status(401);
                res.type("application/json");
                return serializer.toJson(new Message("Error: unauthorized"));
            }



            UserService.LoginRequest loginRequest = serializer.fromJson(req.body(), UserService.LoginRequest.class);
            UserService.LoginResult loginResult = userService.login(loginRequest);
            if (loginResult.message() != null && loginResult.message().contains("Error:")) {
                if (loginResult.message().contains("unauthorized")) {
                    res.status(401);
                } else {
                    res.status(500);
                }
                res.type("application/json");
                return serializer.toJson(new Message(loginResult.message()));
            } else {
                res.status(200);
                res.type("application/json");
                return serializer.toJson(loginResult);
            }
        });
    }
}
