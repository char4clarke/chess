package websocket.messages;

public class NotificationMessage extends ServerMessage {
    private final String message;

    public NotificationMessage(String message) {
        super(ServerMessageType.NOTIFICATION);
        this.serverMessageType = ServerMessageType.NOTIFICATION;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "NotificationMessage{" +
                "message='" + message + '\'' +
                ", serverMessageType=" + serverMessageType +
                '}';
    }
}
