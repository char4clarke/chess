import dataaccess.AuthDAO;
import dataaccess.MySqlAuthDAO;
import dataaccess.MySqlUserDAO;
import dataaccess.UserDAO;
import ui.PreClient;
import ui.ServerFacade;
import service.UserService;

public class ClientMain {
    public static void main(String[] args) {
        var serverUrl = "http://localhost:8080";
        UserDAO userDAO = new MySqlUserDAO();
        AuthDAO authDAO = new MySqlAuthDAO();
        UserService userService = new UserService(userDAO, authDAO);
        ServerFacade serverFacade = new ServerFacade(serverUrl);
        new PreClient(serverFacade).run();
    }
}