package server.websocket;

import chess.ChessBoard;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.*;

import java.lang.reflect.Type;

public class ChessBoardSerializer implements JsonSerializer<ChessBoard> {
    @Override
    public JsonElement serialize(ChessBoard board, Type type, JsonSerializationContext context) {
        JsonArray squares = new JsonArray();
        for (int i = 0; i < 8; i++) {
            JsonArray row = new JsonArray();
            for (int j = 0; j < 8; j++) {
                ChessPiece piece = board.getPiece(new ChessPosition(8 - i, j + 1));
                row.add(context.serialize(piece));
            }
            squares.add(row);
        }
        JsonObject obj = new JsonObject();
        obj.add("squares", squares);
        return obj;
    }
}
