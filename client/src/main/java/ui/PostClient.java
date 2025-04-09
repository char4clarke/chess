package ui;

import exception.ResponseException;
import ui.ServerFacade.*;

import java.util.HashMap;
import java.util.Map;

import java.util.*;

public class PostClient implements ChessClient {
    private final ServerFacade serverFacade;
    private final String authToken;
    private final Map<Integer, Integer> gameIDMap = new HashMap<>();

    public PostClient(ServerFacade serverFacade, String authToken) {
        this.serverFacade = serverFacade;
        this.authToken = authToken;
    }

    @Override
    public void run() {
        System.out.println("Type 'help' for available commands.");
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

        try {
            switch (cmd) {
                case "help" -> displayHelp();
                case "create" -> handleCreateGame(tokens);
                case "list" -> handleListGames();
                case "join" -> handleJoinGame(tokens);
                case "observe" -> handleObserveGame(tokens);
                case "logout" -> {}
                default -> System.out.println("Unknown command. Type 'help' for possible commands.");
            }
        } catch (ResponseException e) {
            handleResponseException(e);
        }
    }

    private void handleResponseException(ResponseException e) {
        int statusCode = e.statusCode();

        switch (statusCode) {
            case 400:
                System.out.println("Bad Request, try again.");
                break;
            case 401:
                System.out.println("Unauthorized, try again.");
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
        if (tokens.length != 2) {
            System.out.println("Invalid arguments. create expects: create <NAME>");
            return;
        }

        String gameName = tokens[1];
        CreateGameRequest request = new CreateGameRequest(gameName);
        CreateGameResult result = serverFacade.createGame(request, authToken);
        if (result != null && result.gameID() != null) {
            System.out.println("Game created successfully with ID: " + result.gameID());
            handleListGames();
        } else {
            System.out.println("Failed to create game");
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
            System.out.println("Invalid arguments. join expects: join <ID> [WHITE|BLACK]");
            return;
        }

        try {
            Integer gameIndex = Integer.parseInt(tokens[1]);
            String playerColor = tokens[2].toUpperCase();

            if (!playerColor.equals("WHITE") && !playerColor.equals("BLACK")) {
                System.out.println("Invalid color. Must be 'WHITE' or 'BLACK'.");
                return;
            }

            if (!gameIDMap.containsKey(gameIndex)) {
                System.out.println("That gameID does not exist, try again.");
                return;
            }

            Integer gameID = gameIDMap.get(gameIndex);
            if (gameID == null) {
                System.out.println("That gameID does not exist, try again.");
                return;
            }
            JoinGameRequest request = new JoinGameRequest(playerColor, gameID);
            JoinGameResult result = serverFacade.joinGame(request, authToken);

            if (result != null && result.message().contains("Success")) {
                System.out.printf("Joined game %d as %s.%n", gameID, playerColor);

                String url = serverFacade.getServerUrl();
                new GameplayClient(url, authToken, gameID, null).start();
//                ChessBoardDrawing.drawChessboard(playerColor.equalsIgnoreCase("BLACK"));

            } else {
                String message = result.message().toLowerCase();
                if (message.contains("already taken")) {
                    System.out.println("Cannot join as " + playerColor.toLowerCase() + " player, spot already taken.");
                } else if (message.contains("full")) {
                    System.out.println("Game is full.");
                } else {
                    System.out.println("Unable to join the game.");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("gameID must be a number.");
        }
    }

    private void handleObserveGame(String[] tokens) {
        System.out.println("Observing game from white team's perspective.");
        if (tokens.length != 2) {
            System.out.println("Invalid arguments. observe expects: observe <ID>");
            return;
        }
        ChessBoardDrawing.drawChessboard(false);

    }

    private void handleLogout() {
        try {
            LogoutRequest request = new LogoutRequest(authToken);
            serverFacade.logout(request);
            System.out.println("Logged out successfully!");
            new PreClient(serverFacade).run();
        } catch (ResponseException e) {
            System.out.println(e.getMessage());
        }
    }
}
