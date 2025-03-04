package chess;

import java.util.Collection;
import java.util.ArrayList;

public class MoveHelper {
    // checks if move is in the bounds of the 8x8 board
    public static boolean isInBounds(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    public static boolean handleTargetSquare(ChessBoard board, ChessPosition myPosition,
                                             ChessPosition newPosition, ChessGame.TeamColor teamColor,
                                             Collection<ChessMove> validMoves) {
        ChessPiece targetSquare = board.getPiece(newPosition);
        // if move is empty square
        if (targetSquare == null) {
            validMoves.add(new ChessMove(myPosition, newPosition, null));
            return true;
        } else {
            // if move is occupied by the other team
            if (!targetSquare.getTeamColor().equals(teamColor)) {
                validMoves.add(new ChessMove(myPosition, newPosition, null));
            }
            return false;
        }
    }

    // adds all moves in a given direction (all diagonal directions for bishop...) to validMoves
    public static Collection<ChessMove> calculateDirectionalMoves(ChessBoard board, ChessPosition myPosition,
                                                                  int[][] directions, ChessGame.TeamColor teamColor) {
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
