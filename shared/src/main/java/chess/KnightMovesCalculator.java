package chess;

import java.util.Collection;
import java.util.ArrayList;

public class KnightMovesCalculator implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        int[][] knightJumps = {
                {-2, -1}, {-2, 1}, {2, -1}, {2, 1},
                {-1, -2}, {-1, 2}, {1, -2}, {1, 2}
        };

        for (int[] jump : knightJumps) {
            int newRow = myPosition.getRow() + jump[0];
            int newCol = myPosition.getColumn() + jump[1];

            if (MoveHelper.isInBounds(newRow, newCol)) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                MoveHelper.handleTargetSquare(board, myPosition, newPosition, board.getPiece(myPosition).getTeamColor(), validMoves);
            }
        }
        return validMoves;
    }
}