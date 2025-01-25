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
            addPromotionOrRegularMove(validMoves, myPosition, forwardOne, teamColor);

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
                    addPromotionOrRegularMove(validMoves, myPosition, newPosition, teamColor);
                }
            }
        }
        return validMoves;
    }
    private void addPromotionOrRegularMove(Collection<ChessMove> validMoves, ChessPosition start, ChessPosition end, ChessGame.TeamColor teamColor) {
        int promotionRank = (teamColor == ChessGame.TeamColor.WHITE) ? 8 : 1;

        if (end.getRow() == promotionRank) {
            validMoves.add(new ChessMove(start, end, ChessPiece.PieceType.QUEEN));
            validMoves.add(new ChessMove(start, end, ChessPiece.PieceType.ROOK));
            validMoves.add(new ChessMove(start, end, ChessPiece.PieceType.BISHOP));
            validMoves.add(new ChessMove(start, end, ChessPiece.PieceType.KNIGHT));
        } else {
            validMoves.add(new ChessMove(start, end, null));
        }
    }
}