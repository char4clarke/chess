package server.websocket;

import chess.*;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dataaccess.DataAccessException;
import model.AuthData;
import service.GameService;
import service.UserService;
import com.google.gson.Gson;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.commands.*;
import websocket.deserializers.*;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {
    private static GameService gameService;
    private static UserService userService;

    private final Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(
            RuntimeTypeAdapterFactory.of(ServerMessage.class, "serverMessageType")
                                .registerSubtype(LoadGameMessage.class, "LOAD_GAME")
                                .registerSubtype(ErrorMessage.class, "ERROR")
                                .registerSubtype(NotificationMessage.class, "NOTIFICATION")
                )
                        .registerTypeAdapter(ChessGame.class, new ChessGameDeserializer())
            .registerTypeAdapter(ChessBoard.class, new ChessBoardDeserializer())
            .registerTypeAdapter(ChessPiece.class, new ChessPieceDeserializer())
            .create();
    private final Map<Session, Integer> gameSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, ChessGame> games = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, ConcurrentHashMap<String, Session>> gameConnections = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Session, String> sessionToAuth = new ConcurrentHashMap<>();

    public static void setServices(GameService gameService, UserService userService) {
        WebSocketHandler.gameService = gameService;
        WebSocketHandler.userService = userService;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("WebSocket connected: " + session.getRemoteAddress());
        gameSessions.put(session, 0);
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        Integer gameID = gameSessions.get(session);
        if (gameID != null) { // Add null check
            gameConnections.get(gameID).remove(sessionToAuth.get(session));
            gameSessions.remove(session);
            sessionToAuth.remove(session);
        }
        String authToken = sessionToAuth.get(session);
        if (authToken != null) {
            gameConnections.get(gameID).remove(authToken);
        }
        sessionToAuth.remove(session);
        gameSessions.remove(session);
        System.out.println("WebSocket closed: " + reason);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        try {
            System.out.println("[WebSocketHandler] Received message: " + message);

            JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
            String commandType = jsonObject.get("commandType").getAsString();
            System.out.println("[WebSocketHandler] Command type: " + commandType);

            UserGameCommand command = null;
            switch (commandType) {
                case "CONNECT" -> command = gson.fromJson(message, ConnectCommand.class);
                case "MAKE_MOVE" -> command = gson.fromJson(message, MakeMoveCommand.class);
                case "LEAVE" -> command = gson.fromJson(message, LeaveCommand.class);
                case "RESIGN" -> command = gson.fromJson(message, ResignCommand.class);
                default -> throw new IllegalArgumentException("Unknown command type: " + commandType);
            }


            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(session, (ConnectCommand) command);
                case MAKE_MOVE -> handleMakeMove(session, (MakeMoveCommand) command);
                case LEAVE -> handleLeave(session, (LeaveCommand) command);
                case RESIGN -> handleResign(session, (ResignCommand) command);
            }
        } catch (Exception e) {
            sendError(session, "Invalid command: " + e.getMessage());
        }
    }


    private void handleConnect(Session session, ConnectCommand command) throws IOException {
        try {
            System.out.println("[WebSocketHandler] Handling CONNECT for game ID: " + command.getGameID());

            userService.validateAuthToken(command.getAuthToken());
            AuthData authData = userService.authDAO.getAuth(command.getAuthToken());
            System.out.println("[WebSocketHandler] Auth validated for user: " + authData.username());

            GameData gameData = gameService.getGame(new GameService.GetGameRequest(command.getGameID())).game();
            System.out.println("[WebSocketHandler] Retrieved GameData: " + gameData);

            if (gameData == null || gameData.game() == null) {
                System.err.println("[WebSocketHandler] Game data is null for ID: " + command.getGameID());
                sendError(session, "Game not found");
                return;
            }


            gameSessions.put(session, command.getGameID());
            sessionToAuth.put(session, command.getAuthToken());
            gameConnections
                    .computeIfAbsent(command.getGameID(), k -> new ConcurrentHashMap<>())
                    .put(command.getAuthToken(), session);


            System.out.println("[WebSocketHandler] Sending LoadGameMessage for game ID: " + command.getGameID());
            sendMessage(session, new LoadGameMessage(gameData.game()));

            String playerRole = (command.getPlayerColor() != null ? String.valueOf(command.getPlayerColor()) : "observer");
            String notification = authData.username() + " joined as " + playerRole;
            broadcast(command.getGameID(), new NotificationMessage(notification), command.getAuthToken());

        } catch (Exception e) {
            System.err.println("[WebSocketHandler] Error in handleConnect: " + e.getMessage());
            sendError(session, "Error: " + e.getMessage());
        }
    }


    private void handleMakeMove(Session session, MakeMoveCommand command) throws IOException {
        try {
            userService.validateAuthToken(command.getAuthToken());
            String username = userService.authDAO.getAuth(command.getAuthToken()).username();

            GameData gameData = gameService.getGame(new GameService.GetGameRequest(command.getGameID())).game();
            ChessGame chessGame = gameData.game();

            if (chessGame.isGameOver()) {
                throw new IOException();
            }

            ChessGame.TeamColor playerTeam = null;
            if (username.equals(gameData.whiteUsername())) {
                playerTeam = ChessGame.TeamColor.WHITE;
            } else if (username.equals(gameData.blackUsername())) {
                playerTeam = ChessGame.TeamColor.BLACK;
            } else {
                throw new IOException();
            }

            if (chessGame.getTeamTurn() != playerTeam) {
                throw new IOException();
            }

            ChessPosition start = command.getMove().getStartPosition();
            ChessPiece piece = chessGame.getBoard().getPiece(start);
            if (piece == null || piece.getTeamColor() != playerTeam) {
                throw new IOException();
            }

            chessGame.makeMove(command.getMove());

            GameData updatedGame = new GameData(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    gameData.blackUsername(),
                    gameData.gameName(),
                    chessGame
            );
            gameService.updateGame(command.getGameID(), updatedGame.game());

            broadcastToAll(command.getGameID(), new LoadGameMessage(chessGame));
            String notification = userService.authDAO.getAuth(command.getAuthToken()).username() + " moved " + command.getMove();
            broadcast(command.getGameID(), new NotificationMessage(notification), command.getAuthToken());

        } catch (InvalidMoveException e) {
            sendError(session, e.getMessage());
        } catch (Exception e) {
            sendError(session, "Server error: " + e.getMessage());
        }
    }





private void handleLeave(Session session, LeaveCommand command) throws IOException {
    try {
        AuthData authData = userService.authDAO.getAuth(command.getAuthToken());
        GameData gameData = gameService.gameDAO.getGame(command.getGameID());

        GameData updatedGame = new GameData(
                gameData.gameID(),
                authData.username().equals(gameData.whiteUsername()) ? null : gameData.whiteUsername(),
                authData.username().equals(gameData.blackUsername()) ? null : gameData.blackUsername(),
                gameData.gameName(),
                gameData.game()
        );

        gameService.updateGamePlayers(
                command.getGameID(),
                updatedGame.whiteUsername(),
                updatedGame.blackUsername()
        );

        gameConnections.get(command.getGameID()).remove(command.getAuthToken());
        sessionToAuth.remove(session);
        gameSessions.remove(session);

        broadcast(command.getGameID(),
                new NotificationMessage(authData.username() + " left"),
                command.getAuthToken()
        );
        session.close();

    } catch (Exception e) {
        sendError(session, "Leave failed: " + e.getMessage());
    }
}




    private void handleResign(Session session, ResignCommand command) throws IOException {
        try {
            userService.validateAuthToken(command.getAuthToken());
            AuthData authData = userService.authDAO.getAuth(command.getAuthToken());

            GameData gameData = gameService.getGame(new GameService.GetGameRequest(command.getGameID())).game();



            if (!authData.username().equals(gameData.whiteUsername())
                    && !authData.username().equals(gameData.blackUsername())) {
                throw new IOException();
            }

            if (gameData.game().isGameOver()) {
                throw new IOException();
            }

            ChessGame updatedGame = gameData.game(); // Create copy
            updatedGame.setGameOver(true); // Add this method to ChessGame
            gameService.updateGame(command.getGameID(), new GameData(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    gameData.blackUsername(),
                    gameData.gameName(),
                    updatedGame
            ).game());

            String notification = authData.username() + " has resigned. Game over!";
            broadcastToAll(command.getGameID(), new NotificationMessage(notification));

        } catch (DataAccessException e) {
            sendError(session, e.getMessage());
        }
    }




    private void sendMessage(Session session, ServerMessage message) {
        try {
            if (session != null && session.isOpen()) {
                String json = gson.toJson(message);
                System.out.println("Sending message: " + json);
                session.getRemote().sendString(json);
                System.out.println("[DEBUG] Sent to session " + session.getRemoteAddress() + ": " + json);
            } else {
                System.err.println("Cannot send message: Session closed");
            }
        } catch (IOException e) {
            System.err.println("Failed to send message: " + e.getMessage());
        }
    }


    private void sendError(Session session, String error) throws IOException {
        sendMessage(session, new ErrorMessage(error));
    }

    private void broadcast(int gameID, ServerMessage message, String excludeAuth) {
        var sessions = gameConnections.getOrDefault(gameID, new ConcurrentHashMap<>());
        sessions.forEach((authToken, session) -> {
            if (!authToken.equals(excludeAuth) && session.isOpen()) {
                sendMessage(session, message);
            }
        });

    }



    private void broadcastToAll(int gameID, ServerMessage message) throws IOException {
        var sessions = gameConnections.getOrDefault(gameID, new ConcurrentHashMap<>());
        for (Session session : sessions.values()) {
            if (session.isOpen()) {
                try {
                    session.getRemote().sendString(gson.toJson(message));
                } catch (IOException e) {
                    System.err.println("Error sending message to session: " + e.getMessage());
                }
            }
        }
    }

}
