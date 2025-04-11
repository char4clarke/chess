

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
    public int createGame(String gameName, ChessGame game) throws DataAccessException {
        System.out.println("[MySqlGameDAO] Inserting game: " + gameName);
        String gameJson = gson.toJson(game);
        System.out.println("[MySqlGameDAO] Serialized game state: " + gameJson);
        String statement = "INSERT INTO games (gameName, stateJSON) VALUES (?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, gameName);
            ps.setString(2, gson.toJson(game));
            ps.executeUpdate();
            try (var rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int gameID = rs.getInt(1); // Return generated gameID
                    System.out.println("[MySqlGameDAO] Generated game ID: " + gameID);
                    return gameID;
                }

            }
            throw new DataAccessException("Failed to create game");
        } catch (SQLException e) {
            throw new DataAccessException("Error: " + e.getMessage());
        }
    }


    @Override
    public GameData getGame(int gameID) throws DataAccessException {

        System.out.println("[MySqlGameDAO] Fetching game ID: " + gameID);

        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM games WHERE gameID = ?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameID);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String gameJson = rs.getString("stateJSON");
                        System.out.println("[MySqlGameDAO] Retrieved game JSON: " + gameJson);
                        ChessGame game = new Gson().fromJson(gameJson, ChessGame.class);
                        return new GameData(
                                gameID,
                                rs.getString("whiteUsername"),
                                rs.getString("blackUsername"),
                                rs.getString("gameName"),
                                game
                        );
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving game: " + e.getMessage());
        }




        return null;
    }

    @Override
    public void joinGame(int gameID, String username, String teamColor) throws DataAccessException {
        GameData game = getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Error: game not found");
        }

        String statement;
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
    public void updateGame(GameData gameData) throws DataAccessException {
        String sql = "UPDATE games SET whiteUsername = ?, blackUsername = ?, gameName = ?, stateJson = ? WHERE gameId = ?";
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, gameData.whiteUsername());
            stmt.setString(2, gameData.blackUsername());
            stmt.setString(3, gameData.gameName());
            stmt.setString(4, new Gson().toJson(gameData.game()));
            stmt.setInt(5, gameData.gameID());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update game: " + e.getMessage());
        }
    }

    private GameData readGame(ResultSet rs) throws SQLException {
        int gameID = rs.getInt("gameID");
        String whiteUsername = rs.getString("whiteUsername");
        String blackUsername = rs.getString("blackUsername");
        String gameName = rs.getString("gameName");
        String stateJSON = rs.getString("stateJSON");

        // Deserialize JSON to ChessGame object
        ChessGame chessGame = gson.fromJson(stateJSON, ChessGame.class);
        return new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame);
    }

    private final String[] createStatements = {
            """
        CREATE TABLE IF NOT EXISTS games (
            gameID INT AUTO_INCREMENT PRIMARY KEY,
            whiteUsername VARCHAR(255),
            blackUsername VARCHAR(255),
            gameName VARCHAR(255) NOT NULL,
            stateJSON TEXT NOT NULL
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
        """
    };
}
