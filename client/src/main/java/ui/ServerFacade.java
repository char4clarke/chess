package ui;

import com.google.gson.Gson;
import exception.ResponseException;
import service.ClearService.*;
import service.GameService.*;
import service.UserService.*;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class ServerFacade {
    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(String url) {
        this.serverUrl = url;
    }

    public ClearResult clear() throws ResponseException {
        return makeRequest("DELETE", "/db", null, null, ClearResult.class);
    }

    public RegisterResult register(RegisterRequest request) throws ResponseException {
        return makeRequest("POST", "/user", request, null, RegisterResult.class);
    }

    public LoginResult login(LoginRequest request) throws ResponseException {
        return makeRequest("POST", "/session", request, null, LoginResult.class);
    }

    public void logout(LogoutRequest request) throws ResponseException {
        makeRequest("DELETE", "/session", null, request.authToken(), null);
    }

    public ListGamesResult listGames(String authToken) throws ResponseException {
        return makeRequest("GET", "/game", null, authToken, ListGamesResult.class);
    }

    public CreateGameResult createGame(CreateGameRequest request, String authToken) throws ResponseException {
        return makeRequest("POST", "/game", request, authToken, CreateGameResult.class);
    }

    public JoinGameResult joinGame(JoinGameRequest request, String authToken) throws ResponseException {
        return makeRequest("PUT", "/game", request, authToken, JoinGameResult.class);
    }

    private <T> T makeRequest(String method, String path, Object body, String authToken, Class<T> responseClass) throws ResponseException {
        try {
            URL url = new URI(serverUrl + path).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);

            if (authToken != null) {
                connection.setRequestProperty("Authorization", authToken);
            }

            connection.setDoOutput(true);
            writeBody(body, connection);
            connection.connect();

            int statusCode = connection.getResponseCode();
            System.out.println("HTTP Status: " + statusCode); // Debug log

            if (!isSuccessful(statusCode)) {
                String errorMessage;
                try (InputStream errorStream = connection.getErrorStream()) {
                    errorMessage = new String(errorStream.readAllBytes(), StandardCharsets.UTF_8);
                }
                throw new ResponseException(statusCode, errorMessage);
            }

            return readBody(connection, responseClass);
        } catch (ResponseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    private static void writeBody(Object body, HttpURLConnection connection) throws IOException {
        if (body != null) {
            connection.setRequestProperty("Content-Type", "application/json");
            String json = new Gson().toJson(body);
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(json.getBytes());
            }
        }
    }

    private <T> T readBody(HttpURLConnection connection, Class<T> responseClass) throws IOException {
        if (responseClass == null) return null;

        try (InputStream inputStream = connection.getInputStream()) {
            return new Gson().fromJson(new InputStreamReader(inputStream), responseClass);
        }
    }

    private boolean isSuccessful(int statusCode) {
        return statusCode / 100 == 2;
    }
}
