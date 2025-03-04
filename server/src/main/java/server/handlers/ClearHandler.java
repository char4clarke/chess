package server.handlers;

import static spark.Spark.delete;
import com.google.gson.Gson;
import service.ClearService;

// class to handle the http requests for clear
public class ClearHandler {
    private final Gson serializer = new Gson();

    private record message(String Message) {}

    public ClearHandler(ClearService clearService) {
        delete("/db", (req, res) -> {
            try {
                clearService.clear();
                res.status(200);
                res.type("application/json");
                return "{}";
            }
            catch (Exception e) {
                res.status(500);
                res.type("application/json");
                return serializer.toJson(new message("Error: " + e.getMessage()));
            }
        });
    }

}
