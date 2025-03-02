package dataaccess;

import model.AuthData;

import java.util.HashMap;
import java.util.Map;

public class MemoryAuthDAO implements AuthDAO {

    private Map<String, AuthData> authDataMap;

    public MemoryAuthDAO() {
        this.authDataMap = new HashMap<>();
    }


    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        authDataMap.put(auth.authToken(), auth);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return authDataMap.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        authDataMap.remove(authToken);
    }
}
