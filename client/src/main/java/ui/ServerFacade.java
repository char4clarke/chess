package ui;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import exception.ResponseException;
import model.AuthData;
import model.GameData;

import java.io.*;
import java.net.*;

public class ServerFacade {
    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public void clearDatabase() throws ResponseException {
        makeRequest("DELETE", "/db", null, null);
    }

    public AuthData register(String username, String password, String email) throws ResponseException {
        var request = new JsonObject();
        request.addProperty("username", username);
        request.addProperty("password", password);
        request.addProperty("email", email);
        return makeRequest("POST", "/user", request, AuthData.class);
    }

    public AuthData login(String username, String password) throws ResponseException {
        var request = new JsonObject();
        request.addProperty("username", username);
        request.addProperty("password", password);
        return makeRequest("POST", "/session", request, AuthData.class);
    }

    public void logout(String authToken) throws ResponseException {
        if (authToken != null) {
            makeRequest("DELETE", "/session", null, null);
        }
    }

    public GameData[] listGames(String authToken) throws ResponseException {
        if (authToken != null) {
            return makeRequest("GET", "/game", null, GameData[].class);
        }
        return new GameData[0];
    }

    public int createGame(String authToken, String gameName) throws ResponseException {
        var request = new JsonObject();
        request.addProperty("gameName", gameName);
        if (authToken != null) {
            var response = makeRequest("POST", "/game", request, JsonObject.class);
            return response.get("gameID").getAsInt();
        }
        return 0;
    }

    public void joinGame(String authToken, int gameID, String playerColor) throws ResponseException {
        var request = new JsonObject();
        request.addProperty("gameID", gameID);
        request.addProperty("playerColor", playerColor);
        if (authToken != null) {
            makeRequest("PUT", "/game", request, null);
        }
    }



    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (ResponseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }


    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            try (InputStream respErr = http.getErrorStream()) {
                if (respErr != null) {
                    throw ResponseException.fromJson(respErr);
                }
            }

            throw new ResponseException(status, "other failure: " + status);
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }


    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
