package dataaccess;

import model.GameData;

import java.util.List;

public interface GameDAO {

    List<GameData> listGames() throws DataAccessException;

    int createGame(String gameName) throws DataAccessException;

    GameData getGame(int gameID) throws DataAccessException;

    void joinGame(int GameID, String username) throws DataAccessException;

    void clearGame() throws DataAccessException;
}
