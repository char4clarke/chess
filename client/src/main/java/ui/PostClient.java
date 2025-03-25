package ui;

import dataaccess.DataAccessException;
import exception.ResponseException;
import clientModel.GameData;
import service.GameService.*;
import service.UserService;
import service.UserService.*;

import java.util.HashMap;
import java.util.Map;

import java.util.*;

public class PostClient implements ChessClient {
    private final ServerFacade serverFacade;
    private final UserService userService;
    private final String authToken;
    private final Map<Integer, Integer> gameIDMap = new HashMap<>();

    public PostClient(ServerFacade serverFacade, UserService userService, String authToken) {
        this.serverFacade = serverFacade;
        this.authToken = authToken;
        this.userService = userService;
    }

    @Override
    public void run() {
        System.out.println(" Welcome back! Type 'help' for available commands.");
        Scanner scanner = new Scanner(System.in);
        String command = "";

        while (!command.equalsIgnoreCase("logout")) {
            System.out.print("[LOGGED_IN] >>> ");
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

    private void executeCommand(String command) throws ResponseException {
        String[] tokens = command.split(" ");
        String cmd = tokens[0].toLowerCase();

        switch (cmd) {
            case "help" -> displayHelp();
            case "create" -> handleCreateGame(tokens);
            case "list" -> handleListGames();
            case "join" -> handleJoinGame(tokens);
            case "observe" -> handleObserveGame(tokens);
            case "logout" -> {}
            default -> System.out.println("Unknown command. Type 'help' for possible commands.");
        }
    }

    private void displayHelp() {
        System.out.println("""
                create <NAME>           - a game
                list                    - games
                join <ID> [WHITE|BLACK] - a game
                observe <ID>            - a game
                logout                  - when you are done
                help                    - with possible commands
                """);
    }

    private void handleCreateGame(String[] tokens) throws ResponseException {
        System.out.println("handle create game started");
        if (tokens.length != 2) {
            System.out.println("Error: Invalid arguments. create expects: create <NAME>");
            return;
        }

        String gameName = tokens[1];
        System.out.println("Creating game with name: " + gameName);
        CreateGameRequest request = new CreateGameRequest(gameName);
        System.out.println("Created CreateGameRequest: " + request);
        try {
            if (authToken == null) {
                System.out.println("Error: Unauthorized - no auth token provided.");
                return;
            }
            try {
                userService.validateAuthToken(authToken);
                System.out.println("auth token validated");
            } catch (DataAccessException e) {
                System.out.println("auth token NOT valid");
                throw new RuntimeException(e);
            }
            CreateGameResult result = serverFacade.createGame(request, authToken);
            System.out.println("Received result from createGame: " + result);

            if (result != null) {
                System.out.println("Game ID: " + result.gameID());
                System.out.println("Result Message: " + result.message());
            }

            if (result != null && result.message() != null && result.message().contains("Success")) {
                System.out.println("Game created successfully with ID: " + result.gameID());
                handleListGames();
            } else {
                System.out.println("Error: " + (result != null ? result.message() : "Unknown error"));
            }
        } catch (ResponseException e) {
            System.out.println("Error during game creation: " + e.getMessage());
        }


    }

    private void handleListGames() throws ResponseException {
        ListGamesResult result = serverFacade.listGames(authToken);
        if (result.message().contains("Success")) {
            List<model.GameData> games = result.games();
            gameIDMap.clear();

            if (games == null || games.isEmpty()) {
                System.out.println("No games currently.");
                return;
            }

            System.out.println("Available games:");
            int index = 1;
            for (model.GameData game : games) {
                gameIDMap.put(index, game.gameID());
                System.out.printf("%d. %s (White: %s, Black: %s)%n",
                        index++, game.gameName(),
                        Optional.ofNullable(game.whiteUsername()).orElse("None"),
                        Optional.ofNullable(game.blackUsername()).orElse("None"));
            }
        } else {
            System.out.println(result.message());
        }
    }

    private void handleJoinGame(String[] tokens) throws ResponseException {
        if (tokens.length != 3) {
            System.out.println("Error: Invalid arguments. join expects: join <ID> [WHITE|BLACK]");
            return;
        }

        try {
            Integer gameIndex = Integer.parseInt(tokens[1]);
            String playerColor = tokens[2].toUpperCase();

            if (!gameIDMap.containsKey(gameIndex)) {
                System.out.println("Error: Invalid gameID.");
                return;
            }

            Integer gameID = gameIDMap.get(gameIndex);
            if (gameID == null) {
                System.out.println("Error: Invalid gameID.");
                return;
            }
            JoinGameRequest request = new JoinGameRequest(playerColor, gameID);
            JoinGameResult result = serverFacade.joinGame(request, authToken);

            if (result != null && result.message().contains("Success")) {
                System.out.printf("Joined game %d as %s.%n", gameID, playerColor);
                ChessBoardDrawing.drawChessboard(playerColor.equalsIgnoreCase("BLACK"));
            } else {
                System.out.println(result.message());
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: gameID must be a number.");
        }
    }

    private void handleObserveGame(String[] tokens) {
        System.out.println("Observing functionality has not yet been implemented.");
    }

    private void handleLogout() {
        try {
            LogoutRequest request = new LogoutRequest(authToken);
            serverFacade.logout(request);
            System.out.println("Logged out successfully!");
            new PreClient(serverFacade, userService).run();
        } catch (ResponseException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
