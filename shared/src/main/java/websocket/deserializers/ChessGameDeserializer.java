package websocket.deserializers;

import chess.ChessBoard;
import chess.ChessGame;
import com.google.gson.*;

import java.lang.reflect.Type;

public class ChessGameDeserializer implements JsonDeserializer<ChessGame> {
    @Override
    public ChessGame deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject obj = json.getAsJsonObject();
        ChessBoard board = context.deserialize(obj.get("board"), ChessBoard.class);
        ChessGame.TeamColor teamTurn = ChessGame.TeamColor.valueOf(obj.get("teamTurn").getAsString());
        boolean isGameOver = obj.get("isGameOver").getAsBoolean();

        ChessGame game = new ChessGame();
        game.setBoard(board);
        game.setTeamTurn(teamTurn);
        game.setGameOver(isGameOver);

        System.out.println("[DEBUG] Deserialized ChessGame: " + game);
        return game;
    }
}
