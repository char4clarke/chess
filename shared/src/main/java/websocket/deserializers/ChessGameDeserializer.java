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
        ChessGame game = new ChessGame();

        if (obj.has("board")) {
            game.setBoard(context.deserialize(obj.get("board"), ChessBoard.class));
        }

        if (obj.has("teamTurn")) {
            game.setTeamTurn(ChessGame.TeamColor.valueOf(obj.get("teamTurn").getAsString()));
        }

        return game;
    }
}
