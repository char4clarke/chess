package chess;

import java.util.Collection;
import java.util.ArrayList;

public class MoveHelper {
    public static boolean isInBounds(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    public static boolean handleTargetSquare(ChessBoard board, ChessPosition myPosition, ChessPosition newPosition, ChessGame.TeamColor teamColor, Collection<ChessMove> validMoves) {
        ChessPiece targetPiece = board.getPiece(newPosition);
        if (targetPiece == null) {
            validMoves.add(new ChessMove(myPosition, newPosition, null));
            return true;
        } else {
            if (!targetPiece.getTeamColor().equals(teamColor)) {
                validMoves.add(new ChessMove(myPosition, newPosition, null));
            }
            return false;
        }
    }
    public static Collection<ChessMove> calculateDirectionalMoves(ChessBoard board, ChessPosition myPosition, int[][] directions, ChessGame.TeamColor teamColor) {
        Collection<ChessMove> validMoves = new ArrayList<>();

        for (int[] direction : directions) {
            int row = myPosition.getRow();
            int col = myPosition.getColumn();

            while (true) {
                row += direction[0];
                col += direction[1];

                if (!isInBounds(row, col)) {
                    break;
                }

                ChessPosition newPosition = new ChessPosition(row, col);

                if (!handleTargetSquare(board, myPosition, newPosition, teamColor, validMoves)) {
                    break;
                }
            }
        }

        return validMoves;
    }
}
