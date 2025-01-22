package chess;

import java.util.ArrayList;
import java.util.Collection;

public class PieceMovesCalculator {

    public static Collection<ChessMove> calculateMoves(ChessPiece piece, ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();

        switch (piece.getPieceType()) {
            case KING:
                break;
            case QUEEN:
                break;
            case BISHOP:
                validMoves.addAll(calculateBishopMoves(myPosition, board));
                break;
            case KNIGHT:
                break;
            case ROOK:
                break;
            case PAWN:
                break;
        }

        return validMoves;
    }

    private static Collection<ChessMove> calculateBishopMoves(ChessPosition myPosition, ChessBoard board) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        int[][] directions = {
                {-1, -1}, // Top-left
                {-1, 1},  // Top-right
                {1, -1},  // Bottom-left
                {1, 1}    // Bottom-right
        };

        for (int[] direction : directions) {
            int row = myPosition.getRow();
            int col = myPosition.getColumn();

            while (true) {
                row += direction[0];
                col += direction[1];

                // Check if the position is out of bounds (1-based indexing)
                if (row < 1 || row > 8 || col < 1 || col > 8) {
                    break; // Stop exploring this direction
                }

                ChessPosition newPosition = new ChessPosition(row, col);
                ChessPiece targetPiece = board.getPiece(newPosition);

                if (targetPiece == null) {
                    // Empty square: Add as a valid move
                    validMoves.add(new ChessMove(myPosition, newPosition, null));
                } else {
                    // occupied square
                    if (!targetPiece.getTeamColor().equals(board.getPiece(myPosition).getTeamColor())) {
                        // opponent's piece: add as a valid move and stop
                        validMoves.add(new ChessMove(myPosition, newPosition, null));
                    }
                    break;
                }
            }
        }

        return validMoves;
    }

}
