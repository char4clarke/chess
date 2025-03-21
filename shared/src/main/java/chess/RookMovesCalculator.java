package chess;

import java.util.Collection;

public class RookMovesCalculator implements PieceMovesCalculator {
    private static final int[][] DIRECTIONS = {
            // all possible directions for rook movement
            {0, 1}, {0, -1},
            {1, 0}, {-1, 0}
    };

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece rook = board.getPiece(myPosition);
        return MoveHelper.calculateDirectionalMoves(board, myPosition, DIRECTIONS, rook.getTeamColor());
    }
}