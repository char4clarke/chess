package model;

import chess.ChessGame;

public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {

    public GameData setWhiteUsername(String whiteUsername) {
        return new GameData(gameID, whiteUsername, blackUsername, gameName, game);
    }

    public GameData setBlackUsername(String blackUsername) {
        return new GameData(gameID, whiteUsername, blackUsername, gameName, game);
    }

}
