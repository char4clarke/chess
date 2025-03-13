package dataaccess;


import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DBGameTests {
    private static MySqlGameDAO gameDAO;

    @BeforeEach
    public void setup() throws DataAccessException {
        gameDAO = new MySqlGameDAO();
        gameDAO.clear();
    }





}
