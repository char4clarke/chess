package dataaccess;

import model.UserData;

import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;

import static java.sql.Types.NULL;

public class MySqlUserDAO implements UserDAO {
    public MySqlUserDAO() {
        try {
            configureDatabase();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    private boolean verifyPassword(String password, String hashed) {
        return BCrypt.checkpw(password, hashed);
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {

    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return null;
    }

    @Override
    public void clear() throws DataAccessException {

    }

    private int executeUpdate(String statement, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    if (param instanceof String p) ps.setString(i + 1, p);
                    else if (param instanceof Integer p) ps.setInt(i + 1, p);
                    else if (param == null) ps.setNull(i + 1, NULL);
                }
                return ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }


    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS auth (
                username VARCHAR(256) NOT NULL,
                password VARCHAR(256) NOT NULL,
                email VARCHAR (256),
                PRIMARY KEY (username),
                INDEX(email)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLAtE=utf8mb4_0900_ai_ci
            """
    };




    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }
}
