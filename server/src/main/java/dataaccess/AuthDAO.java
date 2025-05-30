package dataaccess;

import model.AuthData;

// abstract interface to be able to later add database functionality
public interface AuthDAO {

    void createAuth(AuthData auth) throws DataAccessException;

    AuthData getAuth(String authToken) throws DataAccessException;

    void deleteAuth(String authToken) throws DataAccessException;

    void clear() throws DataAccessException;

    String createAuthToken(String username) throws DataAccessException;

}
