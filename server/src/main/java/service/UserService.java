package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.UserData;
import model.AuthData;
import java.util.UUID;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public record RegisterRequest(String username, String password, String email) {}
    public record RegisterResult(String username, String authToken, String message) {}

    public record LoginRequest(String username, String password) {}
    public record LoginResult(String username, String authToken, String message) {}

    public record LogoutRequest(String authToken) {}

    private String generateToken() {
        return UUID.randomUUID().toString();
    }


    public RegisterResult register(RegisterRequest registerRequest) {
        if (registerRequest.username == null || registerRequest.password == null || registerRequest.email == null) {
            return new RegisterResult(null, null, "Error: Invalid input");
        }

        try {
            UserData u = userDAO.getUser(registerRequest.username);
            if (u != null) {
                return new RegisterResult(null, null, "Error: Username already taken");
            }
        }
        catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        try {
            UserData newUser = new UserData(registerRequest.username, registerRequest.password, registerRequest.email);
            userDAO.createUser(newUser);

            String authToken = generateToken();
            AuthData newAuth = new AuthData(authToken, registerRequest.username());
            authDAO.createAuth(newAuth);

            return new RegisterResult(registerRequest.username, authToken, "Success");
        }
        catch (DataAccessException e) {
            return new RegisterResult(null, null, "Error: " + e.getMessage());
        }

    }


    public LoginResult login(LoginRequest loginRequest) {
        if (loginRequest.username == null || loginRequest.password == null) {
            return new LoginResult(null, null, "Error: Invalid input");
        }

        try {
            boolean isValid = userDAO.validateUser(loginRequest.username(), loginRequest.password());
            UserData user = userDAO.getUser(loginRequest.username);
            if (user == null) {
                return new LoginResult(null, null, "Error: unauthorized");
            } else if (!isValid) {
                return new LoginResult(null, null, "Error: unauthorized");
            }

            String authToken = generateToken();
            AuthData newAuth = new AuthData(authToken, loginRequest.username);
            authDAO.createAuth(newAuth);

            return new LoginResult(loginRequest.username, authToken, "Success");
        }
        catch (DataAccessException e) {
            return new LoginResult(null, null, "Error: " + e.getMessage());
        }

    }
    public void logout(LogoutRequest logoutRequest) throws DataAccessException {
        if (logoutRequest.authToken == null) {
            throw new DataAccessException("Error: Invalid auth token");
        }
        authDAO.deleteAuth(logoutRequest.authToken());
    }

    public void validateAuthToken(String authToken) throws DataAccessException {
        if (authToken == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null || authData.username() == null) {
            throw new DataAccessException("Error: unauthorized");
        }
    }
}
