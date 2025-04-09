package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.List;

// abstract interface to be able to later add database functionality
public interface GameDAO {

    List<GameData> listGames() throws DataAccessException;

    int createGame(String gameName, ChessGame game) throws DataAccessException;

    GameData getGame(int gameID) throws DataAccessException;

    void joinGame(int gameID, String username, String teamColor) throws DataAccessException;

    void clear() throws DataAccessException;

}
