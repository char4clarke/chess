package chess;

import java.util.Collection;
import java.util.ArrayList;

public class PawnMovesCalculator implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        ChessPiece pawn = board.getPiece(myPosition);
        ChessGame.TeamColor teamColor = pawn.getTeamColor();

        int direction = (teamColor == ChessGame.TeamColor.WHITE) ? 1 : -1;

        ChessPosition forwardOne = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn());
        if (MoveHelper.isInBounds(forwardOne.getRow(), forwardOne.getColumn()) && board.getPiece(forwardOne) == null) {
            validMoves.add(new ChessMove(myPosition, forwardOne, null));

            int startingRow = (teamColor == ChessGame.TeamColor.WHITE) ? 2 : 7;

            if (myPosition.getRow() == startingRow) {
                ChessPosition forwardTwo = new ChessPosition(myPosition.getRow() + 2 * direction, myPosition.getColumn());
                if (board.getPiece(forwardTwo) == null) {
                    validMoves.add(new ChessMove(myPosition, forwardTwo, null));
                }
            }
        }
        int[][] diagonals = {
                {direction, -1}, {direction, 1}
        };
        for (int[] offset : diagonals) {
            int newRow = myPosition.getRow() + offset[0];
            int newCol = myPosition.getColumn() + offset[1];

            if (MoveHelper.isInBounds(newRow, newCol)) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece targetPiece = board.getPiece(newPosition);
                if (targetPiece != null && !targetPiece.getTeamColor().equals(teamColor)) {
                    validMoves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
        }
        return validMoves;
    }
}