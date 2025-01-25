package chess;

import java.util.Collection;
import java.util.ArrayList;

public class PawnMovesCalculator implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        ChessPiece pawn = board.getPiece(myPosition);
        ChessGame.TeamColor teamColor = pawn.getTeamColor();

        // figures out which direction the pawn can move based on its color or team
        int direction = (teamColor == ChessGame.TeamColor.WHITE) ? 1 : -1;

        // check if the square directly in front is empty and in bounds
        ChessPosition forwardOne = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn());
        if (MoveHelper.isInBounds(forwardOne.getRow(), forwardOne.getColumn()) && board.getPiece(forwardOne) == null) {
            // add a regular move or a promotion if pawn is at the end
            addPromotionOrRegularMove(validMoves, myPosition, forwardOne, teamColor);


            // if the pawn is in the starting row (based on color) then it can move 2 spaces forward
            int startingRow = (teamColor == ChessGame.TeamColor.WHITE) ? 2 : 7;

            if (myPosition.getRow() == startingRow) {
                ChessPosition forwardTwo = new ChessPosition(myPosition.getRow() + 2 * direction, myPosition.getColumn());
                // checks that both squares are empty before adding as a possible move
                if (board.getPiece(forwardTwo) == null) {
                    validMoves.add(new ChessMove(myPosition, forwardTwo, null));
                }
            }
        }

        // diagonal captures
        int[][] diagonals = {
                // possible diagonal squares relative to pawn's direction
                {direction, -1}, {direction, 1}
        };
        for (int[] offset : diagonals) {
            int newRow = myPosition.getRow() + offset[0];
            int newCol = myPosition.getColumn() + offset[1];

            // check if diagonal move is in bounds
            if (MoveHelper.isInBounds(newRow, newCol)) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece targetPiece = board.getPiece(newPosition);
                // check if there is an opponent piece at the diagonal position and add it to validMoves
                if (targetPiece != null && !targetPiece.getTeamColor().equals(teamColor)) {
                    addPromotionOrRegularMove(validMoves, myPosition, newPosition, teamColor);
                }
            }
        }
        return validMoves;
    }

    // depending on where the target position is, adds either a promotion or a regular move to validMoves
    private void addPromotionOrRegularMove(Collection<ChessMove> validMoves, ChessPosition start, ChessPosition end, ChessGame.TeamColor teamColor) {
        // pawns can promote at row 8 for white, and row 1 for black
        int promotion = (teamColor == ChessGame.TeamColor.WHITE) ? 8 : 1;

        if (end.getRow() == promotion) { // if able to promote, add all possible promotions to validMoves
            validMoves.add(new ChessMove(start, end, ChessPiece.PieceType.QUEEN));
            validMoves.add(new ChessMove(start, end, ChessPiece.PieceType.ROOK));
            validMoves.add(new ChessMove(start, end, ChessPiece.PieceType.BISHOP));
            validMoves.add(new ChessMove(start, end, ChessPiece.PieceType.KNIGHT));
        } else { // add a regular move with null promotion
            validMoves.add(new ChessMove(start, end, null));
        }
    }
}