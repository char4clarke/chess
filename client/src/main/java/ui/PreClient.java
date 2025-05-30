package ui;

import exception.ResponseException;
import ui.ServerFacade.*;

import java.util.Scanner;

public class PreClient implements ChessClient {
    private final ServerFacade serverFacade;

    public PreClient(ServerFacade serverFacade) {
        this.serverFacade = serverFacade;
    }

    @Override
    public void run() {
        System.out.println(" Welcome to 240 chess. Type Help to get started. ");
        Scanner scanner = new Scanner(System.in);
        String command = "";

        while (true) {
            System.out.print("[LOGGED_OUT] >>> ");
            command = scanner.nextLine();

            try {
                if (executeCommand(command)) {
                    break;
                }
            } catch (ResponseException e) {
                handleResponseException(e);
            }
        }

        System.out.println("Goodbye!");
        System.exit(0);
    }

    private void handleResponseException(ResponseException e) {
        int statusCode = e.statusCode();

        switch (statusCode) {
            case 400:
                System.out.println("Bad Request, try again.");
                break;
            case 401:
                System.out.println("Username and password unauthorized, try again.");
                break;
            case 403:
                System.out.println("Already taken, try again.");
                break;
            case 500:
                System.out.println("Hmmm... Try again.");
                break;
            default:
                System.out.println("Hmmm... Try again.");
        }
    }


    private boolean executeCommand(String command) throws ResponseException {
        String[] tokens = command.toLowerCase().split(" ");
        String cmd = tokens[0].toLowerCase();

        switch (cmd) {
            case "help" -> displayHelp();
            case "register" -> handleRegister(tokens);
            case "login" -> handleLogin(tokens);
            case "quit" -> { return true; }
            default -> System.out.println("Unknown command. Type 'help' for possible commands.");
        }
        return false;
    }

    private void displayHelp() {
        System.out.println("""
                register <USERNAME> <PASSWORD> <EMAIL>  - to create an account
                login <USERNAME> <PASSWORD>             - to play chess
                quit                                    - playing chess
                help                                    - with possible commands
                """);
    }

    private void handleRegister(String[] tokens) throws ResponseException {
        if (tokens.length != 4) {
            System.out.println("Invalid argument. register expects: register <USERNAME> <PASSWORD> <EMAIL>");
            return;
        }

        String username = tokens[1];
        String password = tokens[2];
        String email = tokens[3];

        RegisterRequest request = new RegisterRequest(username, password, email);
        RegisterResult result = serverFacade.register(request);

        if (result.message().contains("Success")) {
            System.out.println("Registration successful! Welcome, " + result.username());
            new PostClient(serverFacade, result.authToken()).run();
        } else {
            System.out.println(result.message());
        }
    }


    private void handleLogin(String[] tokens) throws ResponseException {
        if (tokens.length != 3) {
            System.out.println("Invalid argument. login expects: login <USERNAME> <PASSWORD>");
            return;
        }

        String username = tokens[1];
        String password = tokens[2];

        LoginRequest request = new LoginRequest(username, password);
        LoginResult result = serverFacade.login(request);

        if (result.message().contains("Success")) {
            System.out.println("Login successful! Welcome back, " + result.username());
            new PostClient(serverFacade, result.authToken()).run();
        } else {
            System.out.println(result.message());
        }
    }
}
