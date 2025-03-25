//package ui;
//
//import java.io.PrintStream;
//import java.nio.charset.StandardCharsets;
//import static ui.EscapeSequences.*;
//
//public class ChessBoardDrawing {
//    private static final int BOARD_SIZE = 8;
//
//    public static void drawChessboard(boolean isBlackPerspective) {
//        var output = new PrintStream(System.out, true, StandardCharsets.UTF_8);
//        output.print(ERASE_SCREEN);
//
//        if (isBlackPerspective) {
//            drawChessBoardBlack(output);
//        } else {
//            drawChessBoardWhite(output);
//        }
//
//        output.print(RESET_TEXT_COLOR);
//        output.print(RESET_BG_COLOR);
//    }
//
//    private static void drawChessBoardBlack(PrintStream output) {
//        printColumnLabels(output, true);
//        for (int row = 0; row < BOARD_SIZE; row++) {
//            printRow(output, row, true);
//        }
//        printColumnLabels(output, true);
//    }
//
//    private static void drawChessBoardWhite(PrintStream output) {
//        printColumnLabels(output, false);
//        for (int row = BOARD_SIZE - 1; row >= 0; row--) {
//            printRow(output, row, false);
//        }
//        printColumnLabels(output, false);
//    }
//
//    private static void printColumnLabels(PrintStream output, boolean isBlackPerspective) {
//        output.print("  ");
//        for (int col = 0; col < BOARD_SIZE; col++) {
//            char label = (char) ('a' + (isBlackPerspective ? BOARD_SIZE - 1 - col : col));
//            output.printf(" %c ", label);
//        }
//        output.println();
//    }
//
//    private static void printRow(PrintStream output, int row, boolean isBlackPerspective) {
//        int displayRow;
//        if (isBlackPerspective) {
//            displayRow = BOARD_SIZE - row;
//        } else {
//            displayRow = row + 1;
//        }
//        output.printf("%d ", displayRow);
//
//        for (int col = 0; col < BOARD_SIZE; col++) {
//            int actualCol = isBlackPerspective ? col : BOARD_SIZE - 1 - col;
//            boolean isWhiteSquare = (row + col) % 2 == 0;
//            String bgColor = isWhiteSquare ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_DARK_GREY;
//            String piece = getPiece(row, actualCol);
//            output.print(bgColor + piece + RESET_BG_COLOR);
//        }
//        output.printf(" %d%n", displayRow);
//    }
//
//    private static String getPiece(int row, int col) {
//        return switch (row) {
//            case 0 -> switch (col) {
//                case 0, 7 -> BLACK_ROOK;
//                case 1, 6 -> BLACK_KNIGHT;
//                case 2, 5 -> BLACK_BISHOP;
//                case 3 -> BLACK_QUEEN;
//                case 4 -> BLACK_KING;
//                default -> EMPTY;
//            };
//            case 7 -> switch (col) {
//                case 0, 7 -> WHITE_ROOK;
//                case 1, 6 -> WHITE_KNIGHT;
//                case 2, 5 -> WHITE_BISHOP;
//                case 3 -> WHITE_QUEEN;
//                case 4 -> WHITE_KING;
//                default -> EMPTY;
//            };
//            case 1 -> BLACK_PAWN;
//            case 6 -> WHITE_PAWN;
//            default -> EMPTY;
//        };
//    }
//}

package ui;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import static ui.EscapeSequences.*;

public class ChessBoardDrawing {
    private static final int BOARD_SIZE = 8;

    public static void drawChessboard(boolean isBlackPerspective) {
        var output = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        output.print(ERASE_SCREEN);

        if (isBlackPerspective) {
            drawChessBoardBlack(output);
        } else {
            drawChessBoardWhite(output);
        }

        output.print(RESET_TEXT_COLOR);
        output.print(RESET_BG_COLOR);
    }

    private static void drawChessBoardBlack(PrintStream output) {
        printColumnLabels(output, true);
        // Black perspective: rows 1-8 from top to bottom
        for (int dataRow = 0; dataRow < BOARD_SIZE; dataRow++) {
            printRow(output, dataRow, true);
        }
        printColumnLabels(output, true);
    }

    private static void drawChessBoardWhite(PrintStream output) {
        printColumnLabels(output, false);
        // White perspective: rows 8-1 from top to bottom
        for (int dataRow = BOARD_SIZE - 1; dataRow >= 0; dataRow--) {
            printRow(output, dataRow, false);
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

    private static void printRow(PrintStream output, int dataRow, boolean isBlackPerspective) {
        // Universal row number calculation
        int displayRow = dataRow + 1;
        output.printf("%d ", displayRow);

        for (int col = 0; col < BOARD_SIZE; col++) {
            int actualCol = isBlackPerspective ? BOARD_SIZE - 1 - col : col;
            boolean isWhiteSquare = (dataRow + actualCol) % 2 != 0;
            String bgColor = isWhiteSquare ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_DARK_GREY;
            String piece = getPiece(dataRow, actualCol);
            output.print(bgColor + piece + RESET_BG_COLOR);
        }
        output.printf(" %d%n", displayRow);
    }

    private static String getPiece(int physicalRow, int physicalCol) {
        return switch (physicalRow) {
            case 7 -> switch (physicalCol) {  // White's back rank (displayed at bottom for white)
                case 0, 7 -> WHITE_ROOK;
                case 1, 6 -> WHITE_KNIGHT;
                case 2, 5 -> WHITE_BISHOP;
                case 3 -> WHITE_QUEEN;
                case 4 -> WHITE_KING;
                default -> EMPTY;
            };
            case 0 -> switch (physicalCol) {  // Black's back rank (displayed at bottom for black)
                case 0, 7 -> BLACK_ROOK;
                case 1, 6 -> BLACK_KNIGHT;
                case 2, 5 -> BLACK_BISHOP;
                case 3 -> BLACK_QUEEN;
                case 4 -> BLACK_KING;
                default -> EMPTY;
            };
            case 6 -> WHITE_PAWN;  // White pawns (physical row 1)
            case 1 -> BLACK_PAWN;  // Black pawns (physical row 6)
            default -> EMPTY;
        };
    }
}

