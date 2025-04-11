package websocket.messages;

import chess.ChessGame;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LoadGameMessage extends ServerMessage {
    private ChessGame game; // The current state of the chess game

    public LoadGameMessage(ChessGame game) {
        super(ServerMessageType.LOAD_GAME);
        this.game = game;
    }

    public ChessGame getGame() {
        return game;
    }
    public void setGame(ChessGame game) {
        this.game = game;
    }

    @Override
    public ServerMessageType getServerMessageType() {
        return ServerMessageType.LOAD_GAME;
    }

}
