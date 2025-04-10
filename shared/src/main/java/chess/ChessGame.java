package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private TeamColor teamTurn;
    private boolean isGameOver = false;

    public ChessGame() {
        // initialize an empty board and start with white team's turn
        board = new ChessBoard();
        board.resetBoard();
        teamTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && teamTurn == chessGame.teamTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, teamTurn);
    }

    @Override
    public String toString() {
        return "ChessGame{" +
                "board=" + board +
                ", teamTurn=" + teamTurn +
                '}';
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return new ArrayList<>();
        }
        Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();

        for (ChessMove move : moves) {
            if (isMoveValid(move)) {
                validMoves.add(move);
            }
        }
        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null) { // if no piece
            throw new InvalidMoveException("No piece to move.");
        }
        if (piece.getTeamColor() != teamTurn) { // if wrong team's turn
            throw new InvalidMoveException("Not this team's turn.");
        }
        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());
        if (!validMoves.contains(move)) { // if validMoves returns false
            throw new InvalidMoveException("Move is invalid.");
        }

        // if pawn, see if promotion, and add the promotion
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            int promotion = (piece.getTeamColor() == TeamColor.WHITE) ? 8 : 1;
            if (move.getEndPosition().getRow() == promotion) {
                ChessPiece promotionPiece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
                board.addPiece(move.getEndPosition(), promotionPiece);
                board.addPiece(move.getStartPosition(), null);
                teamTurn = (teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
                return;
            }
        }
        // if not pawn, add the normal move
        board.addPiece(move.getEndPosition(), piece);
        board.addPiece(move.getStartPosition(), null);

        // update teamTurn to be the other team's turn
        teamTurn = (teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = getKingPosition(teamColor);
        TeamColor opponent = getOpponentColor(teamColor);

        return isSquareAttacked(kingPosition, opponent);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && !hasValidMoves(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && !hasValidMoves(teamColor);
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    public boolean isMoveValid(ChessMove move) {
        ChessPiece start = board.getPiece(move.getStartPosition());
        ChessPiece end = board.getPiece(move.getEndPosition());

        board.addPiece(move.getEndPosition(), start);
        board.addPiece(move.getStartPosition(), null);

        boolean valid = !isInCheck(start.getTeamColor());

        board.addPiece(move.getStartPosition(), start);
        board.addPiece(move.getEndPosition(), end);

        return valid;
    }

    public ChessPosition getKingPosition(TeamColor teamColor) {
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition position = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    return position;
                }
            }
        }
        return null;
    }

    private boolean hasValidMoves(TeamColor teamColor) {
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition position = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getTeamColor() == teamColor) {
                    if (!validMoves(position).isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isSquareAttacked(ChessPosition position, TeamColor attackerColor) {
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                if (isAttacking(position, new ChessPosition(i, j), attackerColor)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isAttacking(ChessPosition target, ChessPosition attackerPos, TeamColor attackerColor) {
        ChessPiece piece = board.getPiece(attackerPos);
        if (piece == null || piece.getTeamColor() != attackerColor) {
            return false;
        }

        for (ChessMove move : piece.pieceMoves(board, attackerPos)) {
            if (move.getEndPosition().equals(target)) {
                return true;
            }
        }
        return false;
    }

    private TeamColor getOpponentColor(TeamColor teamColor) {
        return (teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    public void setGameOver(boolean gameOver) {
        isGameOver = gameOver;
    }

    public boolean isGameOver() {
        return isGameOver;
    }
}
