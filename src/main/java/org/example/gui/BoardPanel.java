package org.example.gui;


import org.example.common.PieceType;
import org.example.model.Board;
import org.example.model.Piece;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class BoardPanel extends JPanel {
    private Board board;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private BoardClickListener clickListener;
    private PieceType currentPlayer;
    private boolean isMyTurn;

    public interface BoardClickListener {
        void onMove(int fromRow, int fromCol, int toRow, int toCol);
        void onPiecePlacement(int row, int col);
    }

    public BoardPanel() {
        board = new Board();
        currentPlayer = PieceType.EMPTY;
        isMyTurn = false;

        setPreferredSize(new Dimension(400, 400));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isMyTurn) return;

                int cellSize = getWidth() / Board.getBoardSize();
                int row = e.getY() / cellSize;
                int col = e.getX() / cellSize;

                handleClick(row, col);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int cellSize = getWidth() / Board.getBoardSize();

        // Desenha grade
        for (int row = 0; row < Board.getBoardSize(); row++) {
            for (int col = 0; col < Board.getBoardSize(); col++) {
                // Desenha célula
                g.setColor(Color.WHITE);
                g.fillRect(col * cellSize, row * cellSize, cellSize, cellSize);
                g.setColor(Color.BLACK);
                g.drawRect(col * cellSize, row * cellSize, cellSize, cellSize);

                // Desenha peças
                Piece piece = board.getPiece(row, col);
                if (piece.getType() != PieceType.EMPTY) {
                    drawPiece(g, row, col, piece.getType(), cellSize);
                }
            }
        }

        // Destaca célula selecionada
        if (selectedRow != -1) {
            g.setColor(Color.YELLOW);
            g.drawRect(selectedCol * cellSize, selectedRow * cellSize, cellSize, cellSize);
        }

        // Destaca borda do tabuleiro quando for sua vez
        if (isMyTurn) {
            g.setColor(Color.GREEN);
            g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            g.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
        }
    }

    private void drawPiece(Graphics g, int row, int col, PieceType type, int cellSize) {
        int padding = 5;
        int pieceSize = cellSize - (2 * padding);

        if (type == PieceType.PLAYER1) {
            g.setColor(Color.BLACK);
            g.fillOval(col * cellSize + padding, row * cellSize + padding, pieceSize, pieceSize);
        } else if (type == PieceType.PLAYER2) {
            g.setColor(Color.RED);
            g.fillOval(col * cellSize + padding, row * cellSize + padding, pieceSize, pieceSize);
            g.setColor(Color.BLACK);
            g.drawOval(col * cellSize + padding, row * cellSize + padding, pieceSize, pieceSize);
        }
    }

    public void updateBoard(Board newBoard) {
        this.board = newBoard;
        repaint();
    }

    public Board getBoard() {
        return board;
    }

    public void setCurrentPlayer(PieceType player) {
        this.currentPlayer = player;
        repaint();
    }

    public void setMyTurn(boolean isMyTurn) {
        this.isMyTurn = isMyTurn;
        repaint();
    }

    public void setBoardClickListener(BoardClickListener listener) {
        this.clickListener = listener;
    }

    private void handleClick(int row, int col) {
        if (board.isSetupPhase()) {
            if (clickListener != null) {
                clickListener.onPiecePlacement(row, col);
                repaint(); // Força atualização visual após colocação
            }
        } else {
            if (selectedRow == -1) {
                if (board.getPiece(row, col).getType() == currentPlayer) {
                    selectedRow = row;
                    selectedCol = col;
                }
            } else {
                if (clickListener != null) {
                    clickListener.onMove(selectedRow, selectedCol, row, col);
                }
                selectedRow = -1;
                selectedCol = -1;
            }
            repaint(); // Força atualização visual após movimento
        }
    }
}


