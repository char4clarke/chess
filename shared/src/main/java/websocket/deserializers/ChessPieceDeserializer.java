package websocket.deserializers;

import chess.ChessGame;
import chess.ChessPiece;
import com.google.gson.*;

import java.lang.reflect.Type;

public class ChessPieceDeserializer implements JsonDeserializer<ChessPiece> {
    @Override
    public ChessPiece deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        ChessGame.TeamColor color = ChessGame.TeamColor.valueOf(obj.get("pieceColor").getAsString());
        ChessPiece.PieceType type = ChessPiece.PieceType.valueOf(obj.get("type").getAsString());
        return new ChessPiece(color, type);
    }
}
