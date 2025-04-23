package org.example.model;

import org.example.common.PieceType;

import java.util.ArrayList;
import java.util.List;


public class Board {
    private static final int BOARD_SIZE = 5;
    private Piece[][] pieces;
    private boolean isSetupPhase = true; // Fase inicial de colocação das peças
    private int player1PiecesToPlace = 12; // Número de peças para colocar na fase inicial
    private int player2PiecesToPlace = 12;
    private int piecesPlacedThisTurn = 0; // Contador de peças colocadas no turno atual
    private static final int PIECES_PER_TURN = 2; // Número de peças por turno

    public Board() {
        pieces = new Piece[BOARD_SIZE][BOARD_SIZE];
        initializeBoard();
    }

    private void initializeBoard() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                pieces[row][col] = new Piece(PieceType.EMPTY, row, col);
            }
        }
    }

    public boolean isSetupPhase() {
        return isSetupPhase;
    }

    public boolean canPlacePiece(PieceType playerType) {
        if (playerType == PieceType.PLAYER1) {
            return player1PiecesToPlace > 0;
        } else {
            return player2PiecesToPlace > 0;
        }
    }

    public boolean placePiece(int row, int col, PieceType playerType) {
        // Verifica se está na fase de setup e se pode colocar peça
        if (!isSetupPhase || !canPlacePiece(playerType)) {
            return false;
        }

        // Verifica se a posição está vazia
        if (pieces[row][col].getType() != PieceType.EMPTY) {
            return false;
        }

        // Não pode colocar no centro durante a fase inicial
        if (row == BOARD_SIZE/2 && col == BOARD_SIZE/2) {
            return false;
        }

        pieces[row][col].setType(playerType);

        // Atualiza contador de peças
        if (playerType == PieceType.PLAYER1) {
            player1PiecesToPlace--;
        } else {
            player2PiecesToPlace--;
        }

        piecesPlacedThisTurn++;

        // Verifica se a fase de setup terminou
        if (player1PiecesToPlace == 0 && player2PiecesToPlace == 0) {
            isSetupPhase = false;
        }

        return true;
    }

    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, PieceType playerType) {
        // Verifica se está na fase de movimento
        if (isSetupPhase) {
            return false;
        }

        // Verifica se a peça pertence ao jogador
        if (pieces[fromRow][fromCol].getType() != playerType) {
            return false;
        }

        // Verifica se o destino está vazio
        if (pieces[toRow][toCol].getType() != PieceType.EMPTY) {
            return false;
        }

        // Movimento apenas na horizontal ou vertical
        if (fromRow != toRow && fromCol != toCol) {
            return false;
        }

        // Verifica se o movimento é apenas para uma casa adjacente
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        if (rowDiff + colDiff != 1) { // Soma deve ser 1 para movimento de uma casa
            return false;
        }

//        // Verifica se há peças no caminho
//        if (fromRow == toRow) {
//            int start = Math.min(fromCol, toCol);
//            int end = Math.max(fromCol, toCol);
//            for (int col = start + 1; col < end; col++) {
//                if (pieces[fromRow][col].getType() != PieceType.EMPTY) {
//                    return false;
//                }
//            }
//        } else {
//            int start = Math.min(fromRow, toRow);
//            int end = Math.max(fromRow, toRow);
//            for (int row = start + 1; row < end; row++) {
//                if (pieces[row][fromCol].getType() != PieceType.EMPTY) {
//                    return false;
//                }
//            }
//        }

        return true;
    }

    public void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        PieceType type = pieces[fromRow][fromCol].getType();
        pieces[fromRow][fromCol].setType(PieceType.EMPTY);
        pieces[toRow][toCol].setType(type);
    }

    public List<Piece> checkCaptures(int row, int col, PieceType playerType) {
        List<Piece> capturedPieces = new ArrayList<>();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // cima, baixo, esquerda, direita

        for (int[] dir : directions) {
            int r1 = row + dir[0];
            int c1 = col + dir[1];
            int r2 = row + dir[0] * 2;
            int c2 = col + dir[1] * 2;

            if (isValidPosition(r1, c1) && isValidPosition(r2, c2)) {
                Piece middle = pieces[r1][c1];
                Piece end = pieces[r2][c2];

                if (middle.getType() != PieceType.EMPTY &&
                        middle.getType() != playerType &&
                        end.getType() == playerType) {
                    capturedPieces.add(middle);
                }
            }
        }

        return capturedPieces;
    }

    public void removePiece(int row, int col) {
        if (isValidPosition(row, col)) {
            pieces[row][col].setType(PieceType.EMPTY);
        }
    }

    public boolean hasValidMoves(PieceType playerType) {
        // Verifica todas as peças do jogador
        for (int fromRow = 0; fromRow < BOARD_SIZE; fromRow++) {
            for (int fromCol = 0; fromCol < BOARD_SIZE; fromCol++) {
                // Se encontrou uma peça do jogador
                if (pieces[fromRow][fromCol].getType() == playerType) {
                    // Verifica todos os possíveis destinos
                    for (int toRow = 0; toRow < BOARD_SIZE; toRow++) {
                        for (int toCol = 0; toCol < BOARD_SIZE; toCol++) {
                            // Se encontrou um movimento válido
                            if (isValidMove(fromRow, fromCol, toRow, toCol, playerType)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    public Piece getPiece(int row, int col) {
        return pieces[row][col];
    }

    public static int getBoardSize() {
        return BOARD_SIZE;
    }

    public boolean shouldChangeTurn() {
        return piecesPlacedThisTurn >= PIECES_PER_TURN;
    }

    public void resetTurnCounter() {
        piecesPlacedThisTurn = 0;
    }

    public int getPiecesPlacedThisTurn() {
        return piecesPlacedThisTurn;
    }

    public int getPiecesRemainingThisTurn() {
        return PIECES_PER_TURN - piecesPlacedThisTurn;
    }
}