package server.websocket;

import com.google.gson.*;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import chess.*;

import java.lang.reflect.Type;

public class ChessGameSerializer implements JsonSerializer<ChessGame> {

    @Override
    public JsonElement serialize(ChessGame chessGame, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject obj = new JsonObject();
        obj.addProperty("board", chessGame.getBoard().toString());
        obj.addProperty("teamTurn", chessGame.getTeamTurn().toString());
        return obj;
    }
}
