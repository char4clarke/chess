package ui;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static chess.ChessGame.TeamColor.WHITE;
import static chess.ChessPiece.PieceType.*;
import static ui.EscapeSequences.*;
import chess.ChessGame;
import chess.ChessBoard;
import chess.ChessPiece.*;
import chess.ChessPiece;
import chess.ChessPosition;

public class ChessBoardDrawing {
    private static final int BOARD_SIZE = 8;

    public static void drawChessboard(ChessBoard board, boolean isBlackPerspective, Set<ChessPosition> highlights) {
        var output = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        output.print(ERASE_SCREEN);

        if (isBlackPerspective) {
            drawChessBoardBlack(output, board);
        } else {
            drawChessBoardWhite(output, board);
        }

        output.print(RESET_TEXT_COLOR);
        output.print(RESET_BG_COLOR);
    }

    public static void drawChessboardWithHighlights(ChessBoard board, boolean isBlackPerspective, Set<ChessPosition> highlights) {
        var output = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        output.print(ERASE_SCREEN);

        if (isBlackPerspective) {
            printColumnLabels(output, true);
            for (int dataRow = 0; dataRow < BOARD_SIZE; dataRow++) {
                printRowWithHighlights(output, board, dataRow, true, highlights);
            }
            printColumnLabels(output, true);
        } else {
            printColumnLabels(output, false);
            for (int dataRow = BOARD_SIZE - 1; dataRow >= 0; dataRow--) {
                printRowWithHighlights(output, board, dataRow, false, highlights);
            }
            printColumnLabels(output, false);
        }

        output.print(RESET_TEXT_COLOR);
        output.print(RESET_BG_COLOR);
    }

    private static void printRowWithHighlights(PrintStream output, ChessBoard board,
                                               int dataRow, boolean isBlackPerspective, Set<ChessPosition> highlights) {
        int displayRow = dataRow + 1;
        output.printf("%d ", displayRow);

        for (int col = 0; col < BOARD_SIZE; col++) {
            int actualCol = isBlackPerspective ? BOARD_SIZE - 1 - col : col;
            ChessPosition position = new ChessPosition(dataRow + 1, actualCol + 1);

            boolean isHighlighted = highlights != null && highlights.contains(position);
            boolean isWhiteSquare = (dataRow + actualCol) % 2 != 0;

            String bgColor;
            if (isHighlighted) {
                bgColor = SET_BG_COLOR_YELLOW;
            } else {
                bgColor = isWhiteSquare ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_DARK_GREY;
            }

            ChessPiece piece = board.getPiece(position);
            output.print(bgColor + getPieceSymbol(piece) + RESET_BG_COLOR);
        }

        output.printf(" %d%n", displayRow);
    }

    private static void drawChessBoardBlack(PrintStream output, ChessBoard board) {
        printColumnLabels(output, true);
        for (int dataRow = 0; dataRow < BOARD_SIZE; dataRow++) {
            printRow(output, board, dataRow, true);
        }
        printColumnLabels(output, true);
    }

    private static void drawChessBoardWhite(PrintStream output, ChessBoard board) {
        printColumnLabels(output, false);
        for (int dataRow = BOARD_SIZE - 1; dataRow >= 0; dataRow--) {
            printRow(output, board, dataRow, false);
        }
        printColumnLabels(output, false);
    }

    private static void printColumnLabels(PrintStream output, boolean isBlackPerspective) {
        output.print("  ");
        for (int col = 0; col < BOARD_SIZE; col++) {
            char label = (char) ('a' + (isBlackPerspective ? BOARD_SIZE - 1 - col : col));
            output.printf(" %c ", label);
        }
        output.println();
    }

    private static void printRow(PrintStream output, ChessBoard board, int dataRow, boolean isBlackPerspective) {
        int displayRow = dataRow + 1;
        output.printf("%d ", displayRow);

        for (int col = 0; col < BOARD_SIZE; col++) {
            int actualCol = isBlackPerspective ? BOARD_SIZE - 1 - col : col;
            boolean isWhiteSquare = (dataRow + actualCol) % 2 != 0;
            String bgColor = isWhiteSquare ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_DARK_GREY;

            ChessPiece piece = board.getPiece(new ChessPosition(dataRow + 1, actualCol + 1));
            output.print(bgColor + getPieceSymbol(piece) + RESET_BG_COLOR);
        }
        output.printf(" %d%n", displayRow);
    }

    private static String getPieceSymbol(ChessPiece piece) {
        if (piece == null) {
            return EMPTY;
        }
        switch (piece.getPieceType()) {
            case KING:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.BLACK_KING : EscapeSequences.WHITE_KING;
            case QUEEN:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.BLACK_QUEEN : EscapeSequences.WHITE_QUEEN;
            case BISHOP:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.BLACK_BISHOP : EscapeSequences.WHITE_BISHOP;
            case KNIGHT:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.BLACK_KNIGHT : EscapeSequences.WHITE_KNIGHT;
            case ROOK:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.BLACK_ROOK : EscapeSequences.WHITE_ROOK;
            case PAWN:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.BLACK_PAWN : EscapeSequences.WHITE_PAWN;
            default:
                return EscapeSequences.EMPTY;
        }
    }
}
