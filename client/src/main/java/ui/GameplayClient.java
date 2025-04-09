package ui;

import chess.ChessGame;
import exception.ResponseException;
import websocket.messages.*;
import websocket.messages.ServerMessage;


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
}
