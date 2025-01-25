package chess;

import java.util.Collection;

public class QueenMovesCalculator implements PieceMovesCalculator {
    private static final int[][] directions = {
            // all possible directions for queen movement
            {-1, -1}, {-1, 0}, {-1, 1},
            {0, -1},           {0, 1},
            {1, -1},  {1, 0},  {1, 1}
    };

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece queen = board.getPiece(myPosition);
        return MoveHelper.calculateDirectionalMoves(board, myPosition, directions, queen.getTeamColor());
    }
}