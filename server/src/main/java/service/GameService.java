package service;

import chess.ChessBoard;
import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import dataaccess.AuthDAO;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.List;

public class GameService {
    public final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public record CreateGameRequest(String gameName) {}
    public record CreateGameResult(Integer gameID, String message) {}

    public record ListGamesResult(List<GameData> games, String message) {}

    public record GetGameRequest(int gameID) {}
    public record GetGameResult(GameData game, String message) {}

    public record JoinGameRequest(String playerColor, int gameID) {}
    public record JoinGameResult(String message) {
        public JoinGameResult {
            if (message == null) {
                message = "";
            }
        }
    }



    public CreateGameResult createGame(CreateGameRequest createGameRequest, String authToken) {
        if (createGameRequest.gameName == null) {
            return new CreateGameResult(null, "Error: no game name");
        }
        try {
            AuthData authData = authDAO.getAuth(authToken);
            if (authData == null || authData.username() == null) {
                return new CreateGameResult(null, "Error: unauthorized");
            }
        }
        catch (DataAccessException e) {
            return new CreateGameResult(null, "Error: unauthorized");
        }
        try {
            System.out.println("[GameService] Creating game: " + createGameRequest.gameName());
            ChessGame game = new ChessGame();
            ChessBoard board = new ChessBoard();
            board.resetBoard();
            game.setBoard(board);
            System.out.println("[GameService] New game board: " + game.getBoard());

            int gameID = gameDAO.createGame(createGameRequest.gameName, game);
            System.out.println("[GameService] Game created with ID: " + gameID);

            return new CreateGameResult(gameID, "Success");
        }
        catch (DataAccessException e) {
            return new CreateGameResult(null, "Error: " + e.getMessage());
        }
    }


    public ListGamesResult listGames() {
        try {
            List<GameData> games = gameDAO.listGames();
            if (games == null) {
                games = new ArrayList<>();
            }
            return new ListGamesResult(games, "Success");
        }
        catch (DataAccessException e) {
            return new ListGamesResult(new ArrayList<>(), "Error: " + e.getMessage());
        }
    }



    public GetGameResult getGame(GetGameRequest getGameRequest) {
        if (getGameRequest.gameID < 0) {
            return new GetGameResult(null, "Error: invalid gameID");
        }
        try {
            System.out.println("[GameService] Fetching game ID: " + getGameRequest.gameID);

            GameData game = gameDAO.getGame(getGameRequest.gameID);
            if (game == null) {
                return new GetGameResult(null, "Error: invalid gameID");
            }
            System.out.println("[GameService] Retrieved game: " + game);
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
        if (joinGameRequest.gameID() <= 0) {
            return new JoinGameResult("Error: invalid game ID");
        }

        try {
            AuthData authData = authDAO.getAuth(authToken);
            if (authData == null || authData.username() == null) {
                return new JoinGameResult("Error: unauthorized");
            }
            String username = authData.username();

            GameData game = gameDAO.getGame(joinGameRequest.gameID);
            if (game == null) {
                return new JoinGameResult("Error: invalid game ID");
            }

            if (joinGameRequest.playerColor().equalsIgnoreCase("WHITE")) {
                if (game.whiteUsername() != null) {
                    return new JoinGameResult("Error: already taken");
                }
                gameDAO.joinGame(joinGameRequest.gameID, username, joinGameRequest.playerColor());
            } else if (joinGameRequest.playerColor().equalsIgnoreCase("BLACK")) {
                if (game.blackUsername() != null) {
                    return new JoinGameResult("Error: already taken");
                }
                gameDAO.joinGame(joinGameRequest.gameID, username, joinGameRequest.playerColor());
            } else {
                return new JoinGameResult("Error: invalid color");
            }

            return new JoinGameResult("Success");
        }
        catch (DataAccessException e) {
            return new JoinGameResult("Error: " + e.getMessage());
        }
    }

    public void updateGame(int gameID, ChessGame updatedGame) throws DataAccessException {
        GameData existing = gameDAO.getGame(gameID);
        GameData updated = new GameData(
                gameID,
                existing.whiteUsername(),  // Preserve existing players
                existing.blackUsername(),
                existing.gameName(),
                updatedGame
        );
        gameDAO.updateGame(updated);  // Pass full GameData object
    }

    public void updateGamePlayers(int gameID, String white, String black) throws DataAccessException {
        GameData existing = gameDAO.getGame(gameID);
        GameData updated = new GameData(
                gameID,
                white,
                black,
                existing.gameName(),
                existing.game()  // Preserve game state
        );
        gameDAO.updateGame(updated);  // Pass full GameData object
    }

}
