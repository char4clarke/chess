package dataaccess;

import model.UserData;

import java.util.Map;
import java.util.HashMap;

public class MemoryUserDAO implements UserDAO {

    private final Map<String, UserData> userDataMap;

    public MemoryUserDAO() {
        this.userDataMap = new HashMap<>();
    }

    @Override
    public void createUser(UserData user) {
        userDataMap.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) {
        return userDataMap.get(username);
    }

    @Override
    public void clear() throws DataAccessException {
        userDataMap.clear();
    }

}
