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
            return new RegisterResult(null, null, "Invalid input");
        }

        try {
            UserData u = userDAO.getUser(registerRequest.username);
            if (u != null) {
                return new RegisterResult(null, null, "Username already taken");
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
            return new LoginResult(null, null, "Invalid input");
        }

        try {
            UserData user = null;
            user = userDAO.getUser(loginRequest.username);
            if (user == null) {
                return new LoginResult(null, null, "User does not exist");
            } else if (!user.password().equals(loginRequest.password)) {
                return new LoginResult(null, null, "Incorrect password");
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
            throw new DataAccessException("Invalid auth token");
        }
        authDAO.deleteAuth(logoutRequest.authToken());
    }
}
