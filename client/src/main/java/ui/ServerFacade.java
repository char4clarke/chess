package ui;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import service.ClearService.*;
import service.GameService.*;
import service.UserService.*;

import java.io.*;
import java.net.*;

public class ServerFacade {
    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(String url) {
        this.serverUrl = url;
    }

    public ClearResult clear() throws ResponseException {
        return makeRequest("DELETE", "/db", null, ClearResult.class);
    }

    public RegisterResult register(RegisterRequest request) throws ResponseException {
        return makeRequest("POST", "/user", request, RegisterResult.class);
    }

    public LoginResult login(LoginRequest request) throws ResponseException {
        return makeRequest("POST", "/session", request, LoginResult.class);
    }

    public void logout(LogoutRequest request) throws ResponseException {
        if (request.authToken() != null) {
            makeRequest("DELETE", "/session", null, null);
        } else {
            throw new ResponseException(401, "Error: unauthorized");
        }
    }

    public ListGamesResult listGames(String authToken) throws ResponseException {
        if (authToken != null) {
            return makeRequest("GET", "/game", null, ListGamesResult.class);
        } else {
            throw new ResponseException(401, "Error: unauthorized");
        }
    }

    public CreateGameResult createGame(CreateGameRequest request, String authToken) throws ResponseException {
        if (authToken != null) {
            return makeRequest("POST", "/game", request, CreateGameResult.class);
        } else {
            throw new ResponseException(401, "Error: unauthorized");
        }
    }

    public JoinGameResult joinGame(JoinGameRequest request, String authToken) throws ResponseException {
        if (authToken != null) {
            return makeRequest("PUT", "/game", request, null);
        } else {
            throw new ResponseException(401, "Error: unauthorized");
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
