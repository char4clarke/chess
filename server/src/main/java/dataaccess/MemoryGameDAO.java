package dataaccess;

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
    public int createGame(String gameName) {
        int gameID = nextID++;
        GameData game = new GameData(gameID, null, null, gameName, null);
        gameDataMap.put(gameID, game);
        return gameID;
    }

    @Override
    public GameData getGame(int gameID) {
        return gameDataMap.get(gameID);
    }

    @Override
    public void joinGame(int gameID, String username) throws DataAccessException {
        GameData game = gameDataMap.get(gameID);
        if (game == null) {
            throw new DataAccessException("Error: game not found");
        }

        if (game.whiteUsername() == null) {
            game.setWhiteUsername(username);
        } else if (game.blackUsername() == null) {
            game.setBlackUsername(username);
        } else {
            throw new DataAccessException("Error: game is full");
        }

        gameDataMap.put(gameID, game);
    }

    public void updateGame(GameData game) {
        gameDataMap.put(game.gameID(), game);
    }

    @Override
    public void clear() throws DataAccessException {
        gameDataMap.clear();
        nextID = 1;
    }
}
