package chess;

import java.util.Collection;

public class BishopMovesCalculator implements PieceMovesCalculator {
    private static final int[][] directions = {
            // possible directions a bishop can move
            {-1, -1}, {-1, 1},
            {1, -1}, {1, 1}
    };

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece bishop = board.getPiece(myPosition);
        return MoveHelper.calculateDirectionalMoves(board, myPosition, directions, bishop.getTeamColor());
    }
}
