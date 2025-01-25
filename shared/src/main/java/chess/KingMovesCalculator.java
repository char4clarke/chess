package chess;

import java.util.Collection;
import java.util.ArrayList;

public class KingMovesCalculator implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        int[][] kingMoves = {
                // possible king moves
                {-1, -1}, {-1, 0}, {-1, 1},
                {0, -1},           {0, 1},
                {1, -1},  {1, 0},  {1, 1}
        };

        for (int[] move : kingMoves) {
            int newRow = myPosition.getRow() + move[0];
            int newCol = myPosition.getColumn() + move[1];

            // checks if move is in bounds and valid (empty or occupied by an opponent piece) from MoveHelper class
            if (MoveHelper.isInBounds(newRow, newCol)) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                MoveHelper.handleTargetSquare(board, myPosition, newPosition, board.getPiece(myPosition).getTeamColor(), validMoves);
            }
        }
        return validMoves;
    }
}