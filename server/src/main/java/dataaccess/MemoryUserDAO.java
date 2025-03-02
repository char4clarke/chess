package dataaccess;

import model.UserData;

import java.util.Map;
import java.util.HashMap;

public class MemoryUserDAO implements UserDAO {

    private Map<String, UserData> userDataMap;

    public MemoryUserDAO() {
        this.userDataMap = new HashMap<>();
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        userDataMap.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return userDataMap.get(username);
    }

}
