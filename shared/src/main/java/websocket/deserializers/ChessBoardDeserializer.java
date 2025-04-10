package websocket.deserializers;

import chess.ChessBoard;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.*;

import java.lang.reflect.Type;

public class ChessBoardDeserializer implements JsonDeserializer<ChessBoard> {
    @Override
    public ChessBoard deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        ChessBoard board = new ChessBoard();
        JsonObject obj = json.getAsJsonObject();
        JsonArray squares = obj.getAsJsonArray("squares");

        for (int row = 0; row < 8; row++) {
            JsonArray rowArray = squares.get(row).getAsJsonArray();
            for (int col = 0; col < 8; col++) {
                JsonElement pieceElement = rowArray.get(col);
                if (!pieceElement.isJsonNull()) {
                    ChessPiece piece = context.deserialize(pieceElement, ChessPiece.class);
                    board.addPiece(new ChessPosition(row + 1, col + 1), piece);
                }
            }
        }
        return board;
    }
}
