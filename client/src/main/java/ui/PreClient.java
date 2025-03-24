package ui;

import exception.ResponseException;
import service.UserService.*;

import java.util.Scanner;

public class PreClient {
    private final ServerFacade serverFacade;

    public PreClient(ServerFacade serverFacade) {
        this.serverFacade = serverFacade;
    }

    public void run() {
        System.out.println(" Welcome to 240 chess. Type Help to get started. ");
        Scanner scanner = new Scanner(System.in);
        String command = "";

        while (!command.equalsIgnoreCase("quit")) {
            System.out.print("[LOGGED_OUT] >>> ");
            command = scanner.nextLine();

            try {
                executeCommand(command);
            } catch (ResponseException e) {
                System.out.println("Error: " + e);
            }
        }

        System.out.println("Goodbye!");
    }





}
