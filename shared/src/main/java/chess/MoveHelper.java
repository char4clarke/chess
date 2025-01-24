package chess;

import java.util.Collection;
import java.util.ArrayList;

public class MoveHelper {
    public static Collection<ChessMove> calculateDirectionalMoves(ChessBoard board, ChessPosition myPosition, int[][] directions, ChessGame.TeamColor teamColor) {
        Collection<ChessMove> validMoves = new ArrayList<>();

        for (int[] direction : directions) {
            int row = myPosition.getRow();
            int col = myPosition.getColumn();

            while (true) {
                row += direction[0];
                col += direction[1];

                if (row < 1 || row > 8 || col < 1 || col > 8) {
                    break;
                }

                ChessPosition newPosition = new ChessPosition(row, col);
                ChessPiece targetPiece = board.getPiece(newPosition);

                if (targetPiece == null) {
                    validMoves.add(new ChessMove(myPosition, newPosition, null));
                } else {
                    if (!targetPiece.getTeamColor().equas(teamColor)) {
                        validMoves.add(new ChessMove(myPosition, newPosition, null));
                    }
                    break;
                }
            }
        }

        return validMoves;
    }
}
