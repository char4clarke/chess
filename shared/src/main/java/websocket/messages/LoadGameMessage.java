package websocket.messages;

import chess.ChessGame;
import com.google.gson.annotations.Expose;

public class LoadGameMessage extends ServerMessage {
    @Expose
    private final ChessGame game; // The current state of the chess game

    public LoadGameMessage(ChessGame game) {
        super(ServerMessageType.LOAD_GAME);
        this.serverMessageType = ServerMessageType.LOAD_GAME;
        this.game = game;
    }

    public ChessGame getGame() {
        return game;
    }

    @Override
    public String toString() {
        return "LoadGameMessage{" +
                "game=" + game +
                ", serverMessageType=" + serverMessageType +
                '}';
    }
}
