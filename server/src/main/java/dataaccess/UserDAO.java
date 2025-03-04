package dataaccess;

import model.UserData;

// abstract interface to be able to later add database functionality
public interface UserDAO {
    void createUser(UserData user) throws DataAccessException;

    UserData getUser(String username) throws DataAccessException;

    void clear() throws DataAccessException;
}
