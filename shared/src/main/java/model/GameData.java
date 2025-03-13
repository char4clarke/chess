package model;

import chess.ChessGame;

public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {

    public static GameData setWhiteUsername(GameData gameData, String whiteUsername) {
        return new GameData(gameData.gameID(), whiteUsername, gameData.blackUsername(), gameData.gameName(), gameData.game());
    }

    public static GameData setBlackUsername(GameData gameData, String blackUsername) {
        return new GameData(gameData.gameID(), gameData.whiteUsername(), blackUsername, gameData.gameName(), gameData.game());
    }

}
