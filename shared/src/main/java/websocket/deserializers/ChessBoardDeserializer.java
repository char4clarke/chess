package websocket.deserializers;

import chess.ChessBoard;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.*;

import java.lang.reflect.Type;



public class ChessBoardDeserializer implements JsonDeserializer<ChessBoard> {
    @Override
    public ChessBoard deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
        ChessBoard board = new ChessBoard();
        JsonArray squares = json.getAsJsonObject().getAsJsonArray("squares");

        for (int row = 0; row < 8; row++) {
            JsonArray rank = squares.get(row).getAsJsonArray();
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = context.deserialize(rank.get(col), ChessPiece.class);
                board.addPiece(new ChessPosition(8 - row, col + 1), piece);
            }
        }
        return board;
    }
}


