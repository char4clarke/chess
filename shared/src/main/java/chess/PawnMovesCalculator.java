package chess;

import java.util.Collection;

public class PawnMovesCalculator implements PieceMovesCalculator {
    private static final int[][] directions = {
            {-1, -1}, {-1, 1},
            {1, -1}, {1, 1}
    };

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece bishop = board.getPiece(myPosition);
        return MoveHelper.calculateDirectionalMoves(board, myPosition, directions, bishop.getTeamColor());
    }
}