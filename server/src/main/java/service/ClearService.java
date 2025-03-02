package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.UserDAO;

public class ClearService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public record ClearResult(String message) {}

    public ClearService(UserDAO userDAO, AuthDAO authDAO, GameDAO gameDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public ClearResult clear() {
        try {
            userDAO.clear();
            authDAO.clear();
            gameDAO.clear();
            return new ClearResult("Success");
        }
        catch (DataAccessException e) {
            return new ClearResult("Error: " + e.getMessage());
        }
    }
}
