package org.example.model;

import org.example.common.PieceType;

import java.util.ArrayList;
import java.util.List;


import java.util.ArrayList;
import java.util.List;

/**
 * Representa o tabuleiro do jogo Seega.
 * Gerencia o estado das peças, regras de movimentação, capturas e fases do jogo.
 */
public class Board {
    // Tamanho fixo do tabuleiro (5x5)
    private static final int BOARD_SIZE = 5;
    // Matriz de peças do tabuleiro
    private Piece[][] pieces;
    // Indica se está na fase inicial de colocação de peças
    private boolean isSetupPhase = true;
    // Quantidade de peças restantes para cada jogador colocar na fase inicial
    private int player1PiecesToPlace = 12;
    private int player2PiecesToPlace = 12;
    // Contador de peças colocadas no turno atual
    private int piecesPlacedThisTurn = 0;
    // Quantidade de peças que cada jogador pode colocar por turno na fase inicial
    private static final int PIECES_PER_TURN = 2;

    /**
     * Construtor: inicializa o tabuleiro vazio
     */
    public Board() {
        pieces = new Piece[BOARD_SIZE][BOARD_SIZE];
        initializeBoard();
    }

    /**
     * Preenche o tabuleiro com peças vazias
     */
    private void initializeBoard() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                pieces[row][col] = new Piece(PieceType.EMPTY, row, col);
            }
        }
    }

    /**
     * Retorna se está na fase de colocação de peças
     */
    public boolean isSetupPhase() {
        return isSetupPhase;
    }

    /**
     * Verifica se o jogador ainda pode colocar peças na fase inicial
     */
    public boolean canPlacePiece(PieceType playerType) {
        if (playerType == PieceType.PLAYER1) {
            return player1PiecesToPlace > 0;
        } else {
            return player2PiecesToPlace > 0;
        }
    }

    /**
     * Tenta colocar uma peça no tabuleiro durante a fase inicial
     * @return true se conseguiu colocar, false caso contrário
     */
    public boolean placePiece(int row, int col, PieceType playerType) {
        // Só pode colocar peça na fase de setup e se ainda houver peças para colocar
        if (!isSetupPhase || !canPlacePiece(playerType)) {
            return false;
        }
        // Só pode colocar em célula vazia
        if (pieces[row][col].getType() != PieceType.EMPTY) {
            return false;
        }
        // Não pode colocar no centro do tabuleiro na fase inicial
        if (row == BOARD_SIZE/2 && col == BOARD_SIZE/2) {
            return false;
        }

        // Coloca a peça
        pieces[row][col].setType(playerType);

        // Atualiza contadores
        if (playerType == PieceType.PLAYER1) {
            player1PiecesToPlace--;
        } else {
            player2PiecesToPlace--;
        }
        piecesPlacedThisTurn++;

        // Se ambos terminaram de colocar, encerra a fase de setup
        if (player1PiecesToPlace == 0 && player2PiecesToPlace == 0) {
            isSetupPhase = false;
        }

        return true;
    }

    /**
     * Verifica se um movimento é válido para o jogador
     */
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, PieceType playerType) {
        // Só pode mover após a fase de setup
        if (isSetupPhase) {
            return false;
        }
        // Só pode mover peças do próprio jogador
        if (pieces[fromRow][fromCol].getType() != playerType) {
            return false;
        }
        // Só pode mover para célula vazia
        if (pieces[toRow][toCol].getType() != PieceType.EMPTY) {
            return false;
        }
        // Só pode mover na horizontal ou vertical (não diagonal)
        if (fromRow != toRow && fromCol != toCol) {
            return false;
        }
        // Só pode mover para uma casa adjacente
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        if (rowDiff + colDiff != 1) {
            return false;
        }
        return true;
    }

    /**
     * Move uma peça de uma posição para outra
     */
    public void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        PieceType type = pieces[fromRow][fromCol].getType();
        pieces[fromRow][fromCol].setType(PieceType.EMPTY);
        pieces[toRow][toCol].setType(type);
    }

    /**
     * Verifica e retorna as peças capturadas após um movimento
     * @return lista de peças capturadas
     */
    public List<Piece> checkCaptures(int row, int col, PieceType playerType) {
        List<Piece> capturedPieces = new ArrayList<>();
        // Direções: cima, baixo, esquerda, direita
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int[] dir : directions) {
            int r1 = row + dir[0];
            int c1 = col + dir[1];
            int r2 = row + dir[0] * 2;
            int c2 = col + dir[1] * 2;

            // Verifica se as posições são válidas
            if (isValidPosition(r1, c1) && isValidPosition(r2, c2)) {
                Piece middle = pieces[r1][c1];
                Piece end = pieces[r2][c2];

                // Captura se: peça do oponente está entre duas do jogador
                if (middle.getType() != PieceType.EMPTY &&
                        middle.getType() != playerType &&
                        end.getType() == playerType) {
                    capturedPieces.add(middle);
                }
            }
        }
        return capturedPieces;
    }

    /**
     * Remove uma peça do tabuleiro
     */
    public void removePiece(int row, int col) {
        if (isValidPosition(row, col)) {
            pieces[row][col].setType(PieceType.EMPTY);
        }
    }

    /**
     * Verifica se o jogador ainda tem movimentos válidos
     */
    public boolean hasValidMoves(PieceType playerType) {
        for (int fromRow = 0; fromRow < BOARD_SIZE; fromRow++) {
            for (int fromCol = 0; fromCol < BOARD_SIZE; fromCol++) {
                if (pieces[fromRow][fromCol].getType() == playerType) {
                    for (int toRow = 0; toRow < BOARD_SIZE; toRow++) {
                        for (int toCol = 0; toCol < BOARD_SIZE; toCol++) {
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

    /**
     * Verifica se uma posição está dentro dos limites do tabuleiro
     */
    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    /**
     * Retorna a peça em uma posição específica
     */
    public Piece getPiece(int row, int col) {
        return pieces[row][col];
    }

    /**
     * Retorna o tamanho do tabuleiro
     */
    public static int getBoardSize() {
        return BOARD_SIZE;
    }

    /**
     * Indica se o turno deve mudar (após colocar 2 peças)
     */
    public boolean shouldChangeTurn() {
        return piecesPlacedThisTurn >= PIECES_PER_TURN;
    }

    /**
     * Reseta o contador de peças colocadas no turno
     */
    public void resetTurnCounter() {
        piecesPlacedThisTurn = 0;
    }

    /**
     * Retorna quantas peças já foram colocadas neste turno
     */
    public int getPiecesPlacedThisTurn() {
        return piecesPlacedThisTurn;
    }

    /**
     * Retorna quantas peças ainda podem ser colocadas neste turno
     */
    public int getPiecesRemainingThisTurn() {
        return PIECES_PER_TURN - piecesPlacedThisTurn;
    }
}