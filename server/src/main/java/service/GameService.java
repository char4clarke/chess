package service;

import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import dataaccess.AuthDAO;

import java.util.List;

public class GameService {
    private static GameDAO gameDAO;
    private AuthDAO authDAO;

    public GameService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public record CreateGameRequest(String gameName) {}
    public record CreateGameResult(Integer gameID, String message) {}

    public record ListGamesResult(List<GameData> allGames, String message) {}

    public record GetGameRequest(int gameID) {}
    public record GetGameResult(GameData game, String message) {}

    public record JoinGameRequest(String playerColor, int gameID) {}
    public record JoinGameResult(String message) {}



    public static CreateGameResult createGame(CreateGameRequest createGameRequest) {

        if (createGameRequest.gameName == null) {
            return new CreateGameResult(null, "Error: Game name is empty");
        }

        try {

            int gameID = gameDAO.createGame(createGameRequest.gameName);
            return new CreateGameResult(gameID, "Success");
        }
        catch (DataAccessException e) {
            return new CreateGameResult(null, "Error: " + e.getMessage());
        }
    }


    public ListGamesResult listGames() {
        try {
            List<GameData> allGames = gameDAO.listGames();
            return new ListGamesResult(allGames, "Success");
        }
        catch (DataAccessException e) {
            return new ListGamesResult(null, "Error: " + e.getMessage());
        }
    }



    public GetGameResult getGame(GetGameRequest getGameRequest) {
        try {
            GameData game = gameDAO.getGame(getGameRequest.gameID);
            return new GetGameResult(game, "Success");
        }
        catch (DataAccessException e) {
            return new GetGameResult(null, "Error: " + e.getMessage());
        }
    }


    public JoinGameResult joinGame(JoinGameRequest joinGameRequest, String authToken) {

        if (joinGameRequest.playerColor == null) {
            return new JoinGameResult("Error: Color is null");
        }

        try {
            AuthData authData = authDAO.getAuth(authToken);
            String username = authData.username();
            gameDAO.joinGame(joinGameRequest.gameID, username);
            return new JoinGameResult("Success");
        }
        catch (DataAccessException e) {
            return new JoinGameResult("Error: " + e.getMessage());
        }
    }

}
