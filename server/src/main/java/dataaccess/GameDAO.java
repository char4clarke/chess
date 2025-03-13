package dataaccess;

import model.GameData;

import java.util.List;

// abstract interface to be able to later add database functionality
public interface GameDAO {

    List<GameData> listGames() throws DataAccessException;

    int createGame(String gameName) throws DataAccessException;

    GameData getGame(int gameID) throws DataAccessException;

    void joinGame(int gameID, String username) throws DataAccessException;

    void clear() throws DataAccessException;

    void updateGame(GameData game) throws DataAccessException;
}
