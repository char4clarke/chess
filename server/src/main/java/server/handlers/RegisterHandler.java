package server.handlers;

import com.google.gson.Gson;
import service.UserService;

import static spark.Spark.post;

// class to handle the http requests for register
public class RegisterHandler {
    private final Gson serializer = new Gson();

    private record Message(String message) {}

    public RegisterHandler(UserService userService) {


        post("/user", (req, res) -> {
            if (req.body() == null) {
                res.status(400);
                res.type("application/json");
                return serializer.toJson(new Message("Error: bad request"));
            }

            UserService.RegisterRequest registerRequest = serializer.fromJson(req.body(), UserService.RegisterRequest.class);
            if (registerRequest.username() == null || registerRequest.password() == null || registerRequest.email() == null) {
                res.status(400);
                res.type("application/json");
                return serializer.toJson(new Message("Error: bad request"));
            }


            UserService.RegisterResult registerResult = userService.register(registerRequest);
            if (registerResult.message() != null && registerResult.message().contains("Error:")) {
                if (registerResult.message().contains("Invalid input")) {
                    res.status(400);
                } else if (registerResult.message().contains("taken")) {
                    res.status(403);
                } else {
                    res.status(500);
                }
            } else {
                res.status(200);
            }


            res.type("application/json");
            return serializer.toJson(registerResult);
        });
    }
}
