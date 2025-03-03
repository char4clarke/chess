package server.handlers;

import com.google.gson.Gson;
import service.UserService;

import static spark.Spark.delete;

public class LogoutHandler {
    private record Message(String message) {}

    private final Gson serializer = new Gson();

    public LogoutHandler(UserService userService) {


        delete("/session", (req, res) -> {
            String authToken = req.headers("authorization");
            if (authToken == null) {
                res.status(401);
                res.type("application/json");
                return serializer.toJson(new Message("Error: unauthorized"));
            }

            try {
                UserService.LogoutRequest logoutRequest = new UserService.LogoutRequest(authToken);
                userService.logout(logoutRequest);

                res.status(200);
                res.type("application/json");
                return "{}";
            }
            catch (Exception e) {
                res.status(500);
                res.type("application/json");
                return serializer.toJson(new Message("Error: " + e.getMessage()));
            }

        });
    }
}
