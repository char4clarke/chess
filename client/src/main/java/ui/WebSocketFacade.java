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
    private final Gson gson;
    private final String authToken;
    private final Integer gameID;
    private final String playerColor;

    public WebSocketFacade(String url, NotificationHandler notificationHandler,
                           String authToken, Integer gameID, String playerColor) throws ResponseException {

        this.gson = new GsonBuilder()
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
        try {
            url = url.replace("http", "ws") + "/ws";
            URI socketURI = new URI(url);
            this.notificationHandler = notificationHandler;
            this.authToken = authToken;
            this.gameID = gameID;
            this.playerColor = playerColor;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
                    notificationHandler.notify(serverMessage);
                    System.out.println("[DEBUG] Received: " + message);
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        sendConnectCommand();
    }

    private void sendConnectCommand() {
        try {
            ConnectCommand command = new ConnectCommand(
                    authToken, gameID, playerColor
            );
            sendMessage(command);
        } catch (ResponseException e) {
            notificationHandler.notify(new
                    ErrorMessage(e.getMessage()));
        }
    }

    public void sendMessage(UserGameCommand command) throws ResponseException {
        try {
            String json = gson.toJson(command);
            this.session.getBasicRemote().sendText(json);
        } catch (IOException ex) {
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

