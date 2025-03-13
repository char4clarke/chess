package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DBAuthTests {
    private static MySqlAuthDAO authDAO;

    @BeforeEach
    public void setup() throws DataAccessException {
        authDAO = new MySqlAuthDAO();
        authDAO.clear();
    }


}
