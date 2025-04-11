package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import exception.ResponseException;
import websocket.commands.*;
import websocket.deserializers.*;
import websocket.messages.ServerMessage;
import websocket.messages.*;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {
    private Session session;
    private final NotificationHandler notificationHandler;
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
            .enableComplexMapKeySerialization()
            .create();

    private final String authToken;
    private final Integer gameID;
    private final String playerColor;

    public WebSocketFacade(String url, NotificationHandler notificationHandler,
                           String authToken, Integer gameID, String playerColor) throws ResponseException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;
            this.authToken = authToken;
            this.gameID = gameID;
            this.playerColor = playerColor;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    System.out.println("[DEBUG] RAW SERVER MESSAGE: " + message);
                    try {
                        ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
                        System.out.println("[DEBUG] DESERIALIZED SERVER MESSAGE: " + serverMessage);
                        notificationHandler.notify(serverMessage);
                    } catch (Exception e) {
                        System.err.println("[ERROR] Failed to parse server message: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(500, ex.getMessage());

        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        this.session = session;
        System.out.println("[WS] Connection established");
        sendConnectCommand();
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("[WS] Connection closed: " + closeReason);
    }



    private void sendConnectCommand() {
        try {
            var command = new ConnectCommand(authToken, gameID);
            sendMessage(command);
        } catch (ResponseException e) {
            e.printStackTrace();

            notificationHandler.notify(new ErrorMessage(e.getMessage()));
        }
    }


    public void sendMessage(UserGameCommand command) throws ResponseException {
        try {
            System.out.println("⬆️ SENDING: " + gson.toJson(command));
            String json = gson.toJson(command);
            this.session.getBasicRemote().sendText(json);
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }


    public interface NotificationHandler {
        void notify(ServerMessage notification);
    }

    public void makeMove(String authToken, int gameID, ChessMove move) throws ResponseException {
        var command = new MakeMoveCommand(authToken, gameID, move);
        sendMessage(command);
    }

    public void leaveGame(String authToken, int gameID) throws ResponseException {
        var command = new LeaveCommand(authToken, gameID);
        sendMessage(command);
    }

    public void resign(String authToken, int gameID) throws ResponseException {
        var command = new ResignCommand(authToken, gameID);
        sendMessage(command);
    }

    
}

