package websocket.commands;

import chess.ChessGame;

public class ConnectCommand extends UserGameCommand {
    ChessGame.TeamColor playerColor;

    public ConnectCommand(String authToken, Integer gameID, String playerColor) {
        super(CommandType.CONNECT, authToken, gameID);
        this.playerColor = ChessGame.TeamColor.valueOf(playerColor);
    }

    public ChessGame.TeamColor getPlayerColor() {
        return playerColor;
    }
}
