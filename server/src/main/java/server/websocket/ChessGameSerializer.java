package server.websocket;

import com.google.gson.*;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import chess.*;

import java.lang.reflect.Type;


public class ChessGameSerializer implements JsonSerializer<ChessGame> {
    @Override
    public JsonElement serialize(ChessGame game, Type type, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.add("board", context.serialize(game.getBoard()));
        obj.addProperty("teamTurn", game.getTeamTurn().name());
        obj.addProperty("isGameOver", game.isGameOver());
        return obj;
    }
}
