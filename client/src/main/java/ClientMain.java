import ui.PreClient;
import ui.ServerFacade;

public class ClientMain {
    public static void main(String[] args) {
        var serverUrl = "http://localhost:8080";
        ServerFacade serverFacade = new ServerFacade(serverUrl);
        new PreClient(serverFacade).run(false);
    }
}
