package server.websocket;

import com.google.gson.GsonBuilder;
import model.AuthData;
import service.GameService;
import service.UserService;
import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.commands.ConnectCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {
    private static GameService gameService;
    private static UserService userService;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(UserGameCommand.class, new UserGameCommandDeserializer())
            .registerTypeAdapter(ChessGame.class, new ChessGameSerializer())
            .create();
    private final ConcurrentHashMap<Integer, ChessGame> games = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, ConcurrentHashMap<String, Session>> gameConnections = new ConcurrentHashMap<>();

    public static void setServices(GameService gameService, UserService userService) {
        WebSocketHandler.gameService = gameService;
        WebSocketHandler.userService = userService;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("WebSocket connected: " + session.getRemoteAddress());
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        removeSessionFromAllGames(session);
        System.out.println("WebSocket closed: " + reason);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        try {
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(session, (ConnectCommand) command);
//                case MAKE_MOVE -> handleMakeMove(session, (MakeMoveCommand) command);
//                case LEAVE -> handleLeave(session, (LeaveCommand) command);
//                case RESIGN -> handleResign(session, (ResignCommand) command);
                case MAKE_MOVE -> {
                }
                case LEAVE -> {
                }
                case RESIGN -> {
                }
            }
        } catch (Exception e) {
            sendError(session, "Invalid command: " + e.getMessage());
        }
    }

    private void handleConnect(Session session, ConnectCommand command) throws IOException {
        try {
            System.out.println("Received CONNECT command: " + command);


            // Validate auth token
            userService.validateAuthToken(command.getAuthToken());
            AuthData authData = userService.authDAO.getAuth(command.getAuthToken());
            System.out.println("Auth data retrieved: " + (authData != null ? authData.username() : "null"));

            GameData gameData = gameService.getGame(new GameService.GetGameRequest(command.getGameID())).game();
            System.out.println("[DEBUG] GameData: " + gameData);
            System.out.println("[DEBUG] ChessGame: " + gameData.game());
            System.out.println("[DEBUG] ChessBoard: " + gameData.game().getBoard());
            System.out.println("Game data retrieved: " + (gameData != null ? gameData.gameID() : "null"));
            if (gameData == null || gameData.game() == null) {
                sendError(session, "Error: game not found");
                return;
            }
            ChessGame chessGame = gameData.game();
            System.out.println("[DEBUG] ChessGame contents:");
            System.out.println("Board: " + chessGame.getBoard().toString());
            System.out.println("Team Turn: " + chessGame.getTeamTurn().name());

            gameConnections.computeIfAbsent(command.getGameID(), k -> new ConcurrentHashMap<>())
                    .put(command.getAuthToken(), session);
            System.out.println("Session added for game " + command.getGameID());

            sendMessage(session, new LoadGameMessage(gameData.game()));

            String playerRole = (command.getPlayerColor() != null ? command.getPlayerColor() : "observer");
            String notification = authData.username() + " joined as " + playerRole;
            broadcast(command.getGameID(), new NotificationMessage(notification), command.getAuthToken());

        } catch (Exception e) {
            sendError(session, "Error: " + e.getMessage());
        }
    }



    // Helper methods for broadcasting and sending messages

    private void sendMessage(Session session, ServerMessage message) {
        try {
            if (session != null && session.isOpen()) {
                String json = gson.toJson(message);
                System.out.println("Sending message: " + json);
                session.getRemote().sendString(json);
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

    private void broadcast(int gameID, ServerMessage message, String excludeAuth) throws IOException {
        var sessions = gameConnections.getOrDefault(gameID, new ConcurrentHashMap<>());
        for (var entry : sessions.entrySet()) {
            if (!entry.getKey().equals(excludeAuth)) {
                sendMessage(entry.getValue(), message);
            }
        }
    }



    private void removeSessionFromAllGames(Session session) {
        gameConnections.forEach((gameID, sessions) -> {
            sessions.entrySet().removeIf(entry -> entry.getValue().equals(session));
            if (sessions.isEmpty()) {
                games.remove(gameID);
                gameConnections.remove(gameID);
            }
        });
    }
}
