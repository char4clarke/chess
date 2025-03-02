package dataaccess;

import model.GameData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryGameDAO implements GameDAO {
    private Map<Integer, GameData> gameDataMap;
    private int nextID;

    public MemoryGameDAO() {
        this.gameDataMap = new HashMap<>();
        this.nextID = 0;
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        return new ArrayList<>(gameDataMap.values());
    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        int gameID = nextID++;

        GameData game = new GameData(gameID, null, null, gameName, null);
        gameDataMap.put(gameID, game);
        return gameID;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
    GameData game = gameDataMap.get(gameID);
    return game;
    }

    @Override
    public void joinGame(int gameID, String username) throws DataAccessException {
        GameData game = gameDataMap.get(gameID);

        if (game.whiteUsername() == null) {
            game.setWhiteUsername(username);
        } else if (game.blackUsername() == null) {
            game.setBlackUsername(username);
        }

        gameDataMap.put(gameID, game);
    }

    @Override
    public void clear() throws DataAccessException {
        gameDataMap.clear();
        nextID = 0;
    }
}
