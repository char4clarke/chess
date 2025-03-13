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

    @Test
    @Order(1)
    @DisplayName("Create Game (Positive)")
    public void createGamePositive() throws DataAccessException {
        int gameID = gameDAO.createGame("test game");
        Assertions.assertTrue(gameID > 0);
    }

    @Test
    @Order(2)
    @DisplayName("Create Game (Negative)")
    public void createGameNegative() {
        try {
            gameDAO.createGame(null);
            Assertions.fail("Error not thrown");
        } catch (DataAccessException e) {
            Assertions.assertTrue(e.getMessage().contains("Error:"));
        }
    }





}
