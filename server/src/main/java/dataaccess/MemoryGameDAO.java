package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// concrete implementation of in memory data access
public class MemoryGameDAO implements GameDAO {
    private final Map<Integer, GameData> gameDataMap = new HashMap<>();
    private int nextID = 1;


    @Override
    public List<GameData> listGames() {
        return new ArrayList<>(gameDataMap.values());
    }


    @Override
    public int createGame(String gameName, ChessGame game) {
        int gameID = nextID++;
        GameData newGame = new GameData(gameID, null, null, gameName, game);
        gameDataMap.put(gameID, newGame);
        return gameID;
    }

    @Override
    public GameData getGame(int gameID) {
        return gameDataMap.get(gameID);
    }

    @Override
    public void joinGame(int gameID, String username, String teamColor) throws DataAccessException {
        GameData game = gameDataMap.get(gameID);
        if (game == null) {
            throw new DataAccessException("Error: game not found");
        }

        if (game.whiteUsername() == null) {
            game.setWhiteUsername(game, username);
        } else if (game.blackUsername() == null) {
            game.setBlackUsername(game, username);
        } else {
            throw new DataAccessException("Error: game is full");
        }

        gameDataMap.put(gameID, game);
    }


    @Override
    public void clear() throws DataAccessException {
        gameDataMap.clear();
        nextID = 1;
    }

    @Override
    public void updateGame(GameData gameData) throws DataAccessException {

    }
}
