package dataaccess;

import model.GameData;

import java.util.List;

public class MySqlGameDAO implements GameDAO {
    @Override
    public List<GameData> listGames() throws DataAccessException {
        return List.of();
    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        return 0;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return null;
    }

    @Override
    public void joinGame(int gameID, String username) throws DataAccessException {

    }

    @Override
    public void clear() throws DataAccessException {

    }

    @Override
    public void updateGame(GameData game) {

    }
}
