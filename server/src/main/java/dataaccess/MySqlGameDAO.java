package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static dataaccess.DatabaseManager.configureDatabase;
import static dataaccess.DatabaseManager.executeUpdate;

public class MySqlGameDAO implements GameDAO {

    private final Gson gson = new Gson();

    public MySqlGameDAO() {
        try {
            configureDatabase(createStatements);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        List<GameData> games = new ArrayList<>();
        String statement = "SELECT * FROM games";

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement);
             var rs = ps.executeQuery()) {

            while (rs.next()) {
                games.add(readGame(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: " + e.getMessage());
        }

        return games;
    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        if (gameName == null) {
            throw new DataAccessException("Error: game name is null");
        }

        String statement = "INSERT INTO games (gameName) VALUES (?)";
        return executeUpdate(statement, gameName);
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        var statement = "SELECT * FROM games WHERE gameID=?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {
            ps.setInt(1, gameID);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return readGame(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: " + e.getMessage());
        }

        return null;
    }

    @Override
    public void joinGame(int gameID, String username, String teamColor) throws DataAccessException {
        GameData game = getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Error: game not found");
        }
        String statement = "";
        if ("WHITE".equalsIgnoreCase(teamColor)) {
            if (game.whiteUsername() != null) {
                throw new DataAccessException("Error: white player already in use");
            }
            statement = "UPDATE games SET whiteUsername=? WHERE gameID=?";
        } else if ("BLACK".equalsIgnoreCase(teamColor)) {
            if (game.blackUsername() != null) {
                throw new DataAccessException("Error: black player already in use");
            }
            statement = "UPDATE games SET blackUsername=? WHERE gameID=?";
        } else {
            throw new DataAccessException("Error: invalid team color");
        }

        executeUpdate(statement, username, gameID);
    }

    @Override
    public void clear() throws DataAccessException {
        var statement = "TRUNCATE TABLE games";
        executeUpdate(statement);
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        var statement = "UPDATE games SET whiteUsername=?, blackUsername=?, stateJSON=? WHERE gameID=?";
        executeUpdate(statement, game.whiteUsername(), game.blackUsername(), gson.toJson(game.game()), game.gameID());
    }

    private GameData readGame(ResultSet rs) throws SQLException {
        int gameID = rs.getInt("gameID");
        String whiteUsername = rs.getString("whiteUsername");
        String blackUsername = rs.getString("blackUsername");
        String gameName = rs.getString("gameName");

        String stateJSONStr = rs.getString("stateJSON");
        Object stateJSONObj = gson.fromJson(stateJSONStr, Object.class);

        return new GameData(gameID, whiteUsername, blackUsername, gameName, (ChessGame) stateJSONObj);
    }




    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS games (
                gameID INT AUTO_INCREMENT PRIMARY KEY,
                whiteUsername VARCHAR(255),
                blackUsername VARCHAR(255),
                gameName VARCHAR(255) NOT NULL,
                stateJSON TEXT
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLAtE=utf8mb4_0900_ai_ci
            """
    };


}
