package ui;

import chess.*;
import exception.ResponseException;
import websocket.commands.MakeMoveCommand;
import websocket.messages.*;
import websocket.messages.ServerMessage;

import java.util.Arrays;
import java.util.Scanner;


public class GameplayClient implements WebSocketFacade.NotificationHandler {
    private volatile boolean gameStateReceived = false;
    private final WebSocketFacade webSocketFacade;
    private ChessGame game;
    private final String playerColor;
    private final Integer gameID;
    private final String authToken;
    private boolean inGame;

    public GameplayClient(String url, String authToken, Integer gameID, String playerColor) throws ResponseException {
        this.webSocketFacade = new WebSocketFacade(url, this::notify, authToken, gameID, playerColor);
        this.playerColor = playerColor;
        this.authToken = authToken;
        this.gameID = gameID;
        this.game = null;
        this.inGame = true;
    }


    @Override
    public void notify(ServerMessage message) {
        System.out.println("[DEBUG] Received server message: " + message);

        switch (message.getServerMessageType()) {
            case LOAD_GAME -> {
                    System.out.println("[DEBUG] Handling LOAD_GAME");
                    loadGame((LoadGameMessage) message);
            }
            case ERROR -> {
                System.out.println("[DEBUG] Handling ERROR");
                error((ErrorMessage) message);
            }
            case NOTIFICATION -> {
                System.out.println("[DEBUG] Handling NOTIFICATION");
                notification((NotificationMessage) message);
            }
            default -> System.out.println("[ERROR] Unknown server message type.");
        }
    }

    private void loadGame(LoadGameMessage message) {

        System.out.println("⬇️ Received game state");

        synchronized(this) {
            this.game = message.getGame();
            this.notifyAll();
        }
        System.out.println("⬇️ RECEIVED GAME: " + (game != null ? "VALID" : "NULL"));

        if (game != null && game.getBoard() != null) {
            System.out.println("[DEBUG] Game state is valid.");
            if (game.isGameOver()) {
                inGame = false;
            } else {
                redrawBoard();
            }
        } else {
            System.err.println("[ERROR] Game state is null or invalid.");
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
            System.out.println("game board: " + game.getBoard());
            ChessBoardDrawing.drawChessboard(game.getBoard(), isBlackPerspective); // Pass the board
        } else {
            ChessBoard defaultBoard = new ChessBoard();
            defaultBoard.resetBoard();
            ChessBoardDrawing.drawChessboard(defaultBoard, false);
            System.out.println("No game state available to draw.");
        }
    }

    public void run() {
        System.out.println("\nStarting gameplay...");

        synchronized(this) {
            try {
                if (game == null) {
                    this.wait(5000);
                }
            } catch (InterruptedException e) {
                System.out.println("Interrupted while waiting for game state");
            }
        }

        Scanner scanner = new Scanner(System.in);
        while (inGame) {
            System.out.print("[GAME] >>> ");
            String input = scanner.nextLine().trim();

            try {
                if (processCommand(input)) break;
            } catch (ResponseException e) {
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
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

            String moveInput = String.join("", Arrays.copyOfRange(tokens, 1, tokens.length));
            ChessMove move = parseMoveInput(moveInput);
            if (!isValidMove(move)) {
                System.out.println("Invalid move. Either not your turn or illegal move.");
                return;
            }
            MakeMoveCommand command = new MakeMoveCommand(authToken, gameID, move);
            webSocketFacade.sendMessage(command);

            redrawBoard();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();

            System.out.println("Invalid move format: " + e.getMessage());
        }

    }

    private ChessPosition parsePosition(String pos) {
        pos = pos.toLowerCase();
        if (pos.length() != 2) throw new IllegalArgumentException("Invalid position format");
        int col = pos.charAt(0) - 'a' + 1;
        int row = Character.getNumericValue(pos.charAt(1));
        return new ChessPosition(row, col);
    }

    private boolean isValidMove(ChessMove move) {
        try {
            if (game.getTeamTurn() != getPlayerTeamColor()) {
                return false;
            }

            return game.validMoves(move.getStartPosition()).contains(move);
        } catch (Exception e) {
            return false;
        }
    }

    private ChessGame.TeamColor getPlayerTeamColor() {
        return "WHITE".equalsIgnoreCase(playerColor) ?
                ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
    }

    private ChessMove parseMoveInput(String moveStr) {
        if (moveStr.length() < 4) throw new IllegalArgumentException("Invalid move format");

        String from = moveStr.substring(0, 2).toLowerCase();
        String to = moveStr.substring(2, 4).toLowerCase();
        ChessPiece.PieceType promotion = null;

        // Handle promotion
        if (moveStr.length() > 4) {
            char promoChar = moveStr.charAt(4);
            promotion = switch(Character.toUpperCase(promoChar)) {
                case 'Q' -> ChessPiece.PieceType.QUEEN;
                case 'R' -> ChessPiece.PieceType.ROOK;
                case 'B' -> ChessPiece.PieceType.BISHOP;
                case 'N' -> ChessPiece.PieceType.KNIGHT;
                default -> throw new IllegalArgumentException("Invalid promotion piece");
            };
        }

        return new ChessMove(
                parsePosition(from),
                parsePosition(to),
                promotion
        );
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

}
