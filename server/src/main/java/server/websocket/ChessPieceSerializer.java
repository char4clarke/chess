package server.websocket;

import chess.ChessPiece;
import com.google.gson.*;

import java.lang.reflect.Type;

public class ChessPieceSerializer implements JsonSerializer<ChessPiece> {
    @Override
    public JsonElement serialize(ChessPiece piece, Type type, JsonSerializationContext context) {
        if (piece == null) return JsonNull.INSTANCE;

        JsonObject obj = new JsonObject();
        obj.addProperty("type", piece.getPieceType().name());
        obj.addProperty("pieceColor", piece.getTeamColor().name());
        return obj;
    }
}
