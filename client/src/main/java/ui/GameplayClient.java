package ui;

import chess.ChessGame;
import exception.ResponseException;
import websocket.messages.*;
import websocket.messages.ServerMessage;

import java.util.Scanner;


public class GameplayClient {
    private final WebSocketFacade webSocketFacade;
    private ChessGame game;
    private final String playerColor;

    public GameplayClient(String url, String authToken, Integer gameID, String playerColor) throws ResponseException {
        this.webSocketFacade = new WebSocketFacade(url, this::handleServerMessage, authToken, gameID, playerColor);
        this.playerColor = playerColor;
    }

    private void handleServerMessage(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case LOAD_GAME -> loadGame((LoadGameMessage) message);
            case ERROR -> error((ErrorMessage) message);
            case NOTIFICATION -> notification((NotificationMessage) message);
        }
    }

    private void loadGame(LoadGameMessage message) {
        this.game = message.getGame();
        redrawBoard();
    }

    private void error(ErrorMessage message) {
        System.out.println("ERROR: " + message.getErrorMessage());
    }

    private void notification(NotificationMessage message) {
        System.out.println("NOTIFICATION: " + message.getMessage());
    }

    private void redrawBoard() {
        if (game != null) {
            boolean isBlackPerspective = playerColor != null && playerColor.equalsIgnoreCase("BLACK");
            ChessBoardDrawing.drawChessboard(isBlackPerspective);
        }
    }

    public void start() {
        System.out.println("\nStarting gameplay...");
        Scanner scanner = new Scanner(System.in);
        while (true) {
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
//            case "move" -> handleMove(tokens);
//            case "leave" -> { handleLeave(); return true; }
//            case "resign" -> handleResign();
//            case "redraw" -> redrawBoard();
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
}
