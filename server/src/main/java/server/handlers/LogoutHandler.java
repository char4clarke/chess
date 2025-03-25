package server.handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import service.UserService;

import static spark.Spark.delete;

// class to handle the http requests for logout
public class LogoutHandler {
    private record Message(String message) {}

    private final Gson serializer = new Gson();
    private final UserService userService;

    public LogoutHandler(UserService userService) {
        this.userService = userService;


        delete("/session", (req, res) -> {
            String authToken = req.headers("authorization");
            if (authToken == null) {
                res.status(401);
                res.type("application/json");
                return serializer.toJson(new Message("Error: unauthorized"));
            }

            try {
                userService.validateAuthToken(authToken);
                UserService.LogoutRequest logoutRequest = new UserService.LogoutRequest(authToken);
                userService.logout(logoutRequest);

                res.status(200);
                res.type("application/json");
                return "{}";
            }
            catch (DataAccessException e) {
                res.status(401);
                res.type("application/json");
                return serializer.toJson(new Message("Error: unauthorized"));
            }
            catch (Exception e) {
                res.status(500);
                res.type("application/json");
                return serializer.toJson(new Message("Error: " + e.getMessage()));
            }

        });
    }
}
