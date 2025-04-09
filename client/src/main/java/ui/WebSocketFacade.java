package ui;

import com.google.gson.Gson;
import exception.ResponseException;
import websocket.commands.ConnectCommand;
import websocket.messages.ServerMessage;
import websocket.messages.*;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {
    private Session session;
    private final NotificationHandler notificationHandler;
    private final Gson gson = new Gson();
    private final String authToken;
    private final Integer gameID;
    private final String playerColor;

    public WebSocketFacade(String url, NotificationHandler notificationHandler,
                           String authToken, Integer gameID, String playerColor) throws ResponseException {
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
            sendMessage(gson.toJson(command));
        } catch (ResponseException e) {
            notificationHandler.notify(new
                    ErrorMessage(e.getMessage()));
        }
    }

    public void sendMessage(String message) throws ResponseException {
        try {
            this.session.getBasicRemote().sendText(message);
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }


    public interface NotificationHandler {
        void notify(ServerMessage notification);
    }
}

