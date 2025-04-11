package org.example;

// SeegaBoard.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SeegaBoard extends JPanel {
    private static final int BOARD_SIZE = 5;
    private static final int CELL_SIZE = 60;
    private Cell[][] board;
    private Color player1Color = Color.BLUE;
    private Color player2Color = Color.RED;
    private boolean isPlayer1Turn = true;
    private String player1Name;
    private String player2Name;
    private GameCallback callback;
    private boolean isMyTurn = false;
    private boolean canPlay = false;

    public interface GameCallback {
        void onMoveMade(int fromRow, int fromCol, int toRow, int toCol);
        void onGameEnd(String winner);
    }

    public SeegaBoard(String player1Name, String player2Name, GameCallback callback) {
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.callback = callback;

        setPreferredSize(new Dimension(BOARD_SIZE * CELL_SIZE, BOARD_SIZE * CELL_SIZE));
        initializeBoard();
        setupMouseListener();
    }

    // Novo método para definir nomes dos jogadores
    public void setPlayerNames(String player1Name, String player2Name) {
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        repaint();
    }

    // Novo método para definir se é a vez do jogador atual
    public void setMyTurn(boolean isMyTurn) {
        this.isMyTurn = isMyTurn;
        this.canPlay = isMyTurn;
        repaint();
    }

    // Novo método para fazer um movimento recebido do servidor
    public void makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (isValidMove(fromRow, fromCol, toRow, toCol)) {
            // Realizar o movimento
            board[toRow][toCol].setOwner(board[fromRow][fromCol].getOwner());
            board[fromRow][fromCol].setOwner(0);

            // Trocar o turno
            isPlayer1Turn = !isPlayer1Turn;

            // Verificar se o jogo acabou
            if (checkGameEnd()) {
                callback.onGameEnd(isPlayer1Turn ? player2Name : player1Name);
            }

            repaint();
        }
    }

    private void initializeBoard() {
        board = new Cell[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = new Cell();
            }
        }
        setupInitialPieces();
    }

    private void setupInitialPieces() {
        // Player 1 (Azul) começa com peças na primeira linha
        for (int j = 0; j < BOARD_SIZE; j++) {
            board[0][j].setOwner(1);
        }

        // Player 2 (Vermelho) começa com peças na última linha
        for (int j = 0; j < BOARD_SIZE; j++) {
            board[BOARD_SIZE-1][j].setOwner(2);
        }
    }

    private Cell selectedCell = null;
    private int selectedRow = -1;
    private int selectedCol = -1;

    private void setupMouseListener() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!canPlay) return; // Não permite jogadas se não for sua vez

                int row = e.getY() / CELL_SIZE;
                int col = e.getX() / CELL_SIZE;

                if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
                    handleCellClick(row, col);
                }
            }
        });
    }

    private void handleCellClick(int row, int col) {
        if (selectedCell == null) {
            // Primeira seleção
            Cell clickedCell = board[row][col];
            if (clickedCell.getOwner() == (isPlayer1Turn ? 1 : 2)) {
                selectedCell = clickedCell;
                selectedRow = row;
                selectedCol = col;
                repaint();
            }
        } else {
            // Segunda seleção (movimento)
            if (isValidMove(selectedRow, selectedCol, row, col)) {
                // Realizar o movimento
                board[row][col].setOwner(selectedCell.getOwner());
                selectedCell.setOwner(0);

                // Notificar sobre o movimento
                callback.onMoveMade(selectedRow, selectedCol, row, col);

                // Verificar se o jogo acabou
                if (checkGameEnd()) {
                    callback.onGameEnd(isPlayer1Turn ? player1Name : player2Name);
                }

                // Trocar o turno
                isPlayer1Turn = !isPlayer1Turn;
                canPlay = false; // Impede jogadas até receber confirmação do servidor
            }

            // Limpar seleção
            selectedCell = null;
            selectedRow = -1;
            selectedCol = -1;
            repaint();
        }
    }

    private boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        // Movimento deve ser para uma célula vazia
        if (board[toRow][toCol].getOwner() != 0) {
            return false;
        }

        // Movimento deve ser ortogonal e adjacente
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);

        return (rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1);
    }

    private boolean checkGameEnd() {
        // Verifica se algum jogador perdeu todas as peças
        boolean hasPlayer1Pieces = false;
        boolean hasPlayer2Pieces = false;

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                int owner = board[i][j].getOwner();
                if (owner == 1) hasPlayer1Pieces = true;
                if (owner == 2) hasPlayer2Pieces = true;
            }
        }

        return !hasPlayer1Pieces || !hasPlayer2Pieces;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Desenhar o tabuleiro
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                // Desenhar célula
                g2d.setColor(Color.WHITE);
                g2d.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);

                // Desenhar peça
                Cell cell = board[i][j];
                if (cell.getOwner() > 0) {
                    g2d.setColor(cell.getOwner() == 1 ? player1Color : player2Color);
                    g2d.fillOval(j * CELL_SIZE + 5, i * CELL_SIZE + 5,
                            CELL_SIZE - 10, CELL_SIZE - 10);
                }
            }
        }

        // Destacar célula selecionada
        if (selectedCell != null) {
            g2d.setColor(Color.YELLOW);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(selectedCol * CELL_SIZE, selectedRow * CELL_SIZE,
                    CELL_SIZE, CELL_SIZE);
        }

        // Indicador de turno
        if (isMyTurn) {
            g2d.setColor(new Color(0, 255, 0, 64)); // Verde semi-transparente
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    public void forfeitGame() {
        callback.onGameEnd(isPlayer1Turn ? player2Name : player1Name);
    }

    public void passTurn() {
        isPlayer1Turn = !isPlayer1Turn;
        canPlay = false;
        repaint();
    }

    private static class Cell {
        private int owner; // 0 = vazio, 1 = player1, 2 = player2

        public Cell() {
            this.owner = 0;
        }

        public int getOwner() {
            return owner;
        }

        public void setOwner(int owner) {
            this.owner = owner;
        }
    }
}