package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import exception.ResponseException;
import websocket.commands.MakeMoveCommand;
import websocket.messages.*;
import websocket.messages.ServerMessage;

import java.util.Scanner;


public class GameplayClient implements WebSocketFacade.NotificationHandler {
    private final WebSocketFacade webSocketFacade;
    private ChessGame game;
    private final String playerColor;
    private final Integer gameID;
    private final String authToken;
    private boolean inGame;

    public GameplayClient(String url, String authToken, Integer gameID, String playerColor) throws ResponseException {
        this.webSocketFacade = new WebSocketFacade(url, this::handleServerMessage, authToken, gameID, playerColor);
        this.playerColor = playerColor;
        this.authToken = authToken;
        this.gameID = gameID;
        this.game = null;
        this.inGame = true;
    }

    private void handleServerMessage(ServerMessage message) {
        System.out.println("Received message type: " + message.getServerMessageType());
        switch (message.getServerMessageType()) {
            case LOAD_GAME -> loadGame((LoadGameMessage) message);
            case ERROR -> error((ErrorMessage) message);
            case NOTIFICATION -> notification((NotificationMessage) message);
        }
    }

    @Override
    public void notify(ServerMessage message) {

        switch (message.getServerMessageType()) {
            case LOAD_GAME -> loadGame((LoadGameMessage) message);
            case ERROR -> error((ErrorMessage) message);
            case NOTIFICATION -> notification((NotificationMessage) message);
        }
    }

    private void loadGame(LoadGameMessage message) {
        System.out.println("Received game state");

        this.game = message.getGame();
        if (game != null && game.getBoard() != null) {
            redrawBoard();
        } else {
            System.out.println("Received empty game state");
        }
    }

    private void error(ErrorMessage message) {
        System.out.println("ERROR: " + message.getErrorMessage());
    }

    private void notification(NotificationMessage message) {
        System.out.println("NOTIFICATION: " + message.getMessage());
    }

    private void redrawBoard() {
        if (game != null) {
            boolean isBlackPerspective = "BLACK".equalsIgnoreCase(playerColor);
            ChessBoardDrawing.drawChessboard(game.getBoard(), isBlackPerspective); // Pass the board
        } else {
            ChessBoard defaultBoard = new ChessBoard();
            defaultBoard.resetBoard();
            ChessBoardDrawing.drawChessboard(defaultBoard, false);
            System.out.println("No game state available to draw.");
        }
    }

    public void start() {
        System.out.println("\nStarting gameplay...");
        redrawBoard();
        Scanner scanner = new Scanner(System.in);
        while (inGame) {
            System.out.print("[GAME] >>> ");
            String input = scanner.nextLine().trim();

            try {
                if (processCommand(input)) break;
            } catch (ResponseException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private boolean processCommand(String input) throws ResponseException {
        String[] tokens = input.split(" ");
        switch (tokens[0].toLowerCase()) {
            case "help" -> displayHelp();
            case "move" -> handleMove(tokens);
            case "leave" -> { handleLeave(); return true; }
            case "resign" -> handleResign();
            case "redraw" -> redrawBoard();
//            case "highlight" -> highlightMoves(tokens);
            default -> System.out.println("Unknown command. Type 'help' for options.");
        }
        return false;
    }

    private void displayHelp() {
        System.out.println("""
            Commands:
              move <FROM> <TO> - Make a chess move (e.g., 'move e2e4')
              redraw           - Redraw the chess board
              leave            - Leave the game
              resign           - Resign from the game
              highlight <SQUARE> - Show legal moves (e.g., 'highlight e2')
              help             - Show this help message
            """);
    }

    private void handleMove(String[] tokens) throws ResponseException {
        if (tokens.length != 3) {
            System.out.println("Invalid command. Usage: move <FROM> <TO>");
            return;
        }

        try {
            ChessPosition start = parsePosition(tokens[1]);
            ChessPosition end = parsePosition(tokens[2]);
            ChessMove move = new ChessMove(start, end, null); // Promotion piece handling can be added

            MakeMoveCommand command = new MakeMoveCommand(authToken, gameID, move);
            webSocketFacade.sendMessage(command);

            redrawBoard();
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid move format: " + e.getMessage());
        }
    }

    private void handleLeave() throws ResponseException {
        System.out.println("Leaving the game...");
        webSocketFacade.leaveGame(authToken, gameID);
        inGame = false;
    }

    private void handleResign() throws ResponseException {
        System.out.print("Are you sure you want to resign? (yes/no) ");
        Scanner scanner = new Scanner(System.in);
        String confirmation = scanner.nextLine().trim();

        if (confirmation.equalsIgnoreCase("yes")) {
            webSocketFacade.resign(authToken, gameID);
            System.out.println("You have resigned from the game");
            inGame = false;
        } else {
            System.out.println("Resignation canceled");
        }
    }

    private ChessPosition parsePosition(String pos) {
        if (pos.length() != 2) throw new IllegalArgumentException("Invalid position format");
        int col = pos.charAt(0) - 'a' + 1;
        int row = Character.getNumericValue(pos.charAt(1));
        return new ChessPosition(row, col);
    }
}
