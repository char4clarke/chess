package websocket.messages;

public class ErrorMessage extends ServerMessage {
    private final String errorMessage;


    public ErrorMessage(String errorMessage) {
        super(ServerMessageType.ERROR);
        this.serverMessageType = ServerMessageType.ERROR;
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return "ErrorMessage{" +
                "errorMessage='" + errorMessage + '\'' +
                ", serverMessageType=" + serverMessageType +
                '}';
    }
}
