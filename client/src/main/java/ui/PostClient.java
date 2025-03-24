package ui;

import exception.ResponseException;

import java.util.HashMap;
import java.util.Map;

import java.util.*;

public class PostClient {
    private final ServerFacade serverFacade;
    private final String authToken;
    private final Map<Integer, Integer> gameIDMap = new HashMap<>();

    public PostClient(ServerFacade serverFacade, String authToken) {
        this.serverFacade = serverFacade;
        this.authToken = authToken;
    }

    public void run() {
        System.out.println(" Welcome back! Type 'help' for available commands.");
        Scanner scanner = new Scanner(System.in);
        String command = "";

        while (!command.equalsIgnoreCase("logout")) {
            System.out.print("[LOGGED_IN} >>> ");
            command = scanner.nextLine();

            try {
                executeCommand(command);
            } catch (ResponseException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        System.out.println("Logging out...");
        handleLogout();
    }
}
