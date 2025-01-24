package chess;

import java.util.Collection;

public class RookMovesCalculator implements PieceMovesCalculator {
    private static final int[][] directions = {
            {0, 1}, {0, -1},
            {1, 0}, {-1, 0}
    };

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece bishop = board.getPiece(myPosition);
        return MoveHelper.calculateDirectionalMoves(board, myPosition, directions, bishop.getTeamColor());
    }
}