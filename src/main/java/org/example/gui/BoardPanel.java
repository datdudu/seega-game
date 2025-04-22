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

        // Definir tamanho mínimo para garantir que o tabuleiro seja visível
        setMinimumSize(new Dimension(400, 400));
        setPreferredSize(new Dimension(400, 400));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isMyTurn) return;

                // Usar o menor valor entre largura e altura para manter células quadradas
                int size = Math.min(getWidth(), getHeight());
                int cellSize = size / Board.getBoardSize();

                // Calcular o offset para centralizar o tabuleiro
                int xOffset = (getWidth() - (cellSize * Board.getBoardSize())) / 2;
                int yOffset = (getHeight() - (cellSize * Board.getBoardSize())) / 2;

                // Calcular a posição do clique considerando o offset
                int row = (e.getY() - yOffset) / cellSize;
                int col = (e.getX() - xOffset) / cellSize;

                // Verificar se o clique foi dentro do tabuleiro
                if (row >= 0 && row < Board.getBoardSize() &&
                        col >= 0 && col < Board.getBoardSize()) {
                    handleClick(row, col);
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Ativar antialiasing para melhor qualidade visual
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Usar o menor valor entre largura e altura para manter células quadradas
        int size = Math.min(getWidth(), getHeight());
        int cellSize = size / Board.getBoardSize();

        // Calcular offset para centralizar o tabuleiro
        int xOffset = (getWidth() - (cellSize * Board.getBoardSize())) / 2;
        int yOffset = (getHeight() - (cellSize * Board.getBoardSize())) / 2;

        // Desenhar fundo do tabuleiro
        g2d.setColor(new Color(240, 240, 240));
        g2d.fillRect(xOffset, yOffset, cellSize * Board.getBoardSize(),
                cellSize * Board.getBoardSize());

        // Desenhar grade e peças
        for (int row = 0; row < Board.getBoardSize(); row++) {
            for (int col = 0; col < Board.getBoardSize(); col++) {
                // Posição atual da célula
                int x = xOffset + (col * cellSize);
                int y = yOffset + (row * cellSize);

                // Desenhar célula
                g2d.setColor(Color.WHITE);
                g2d.fillRect(x, y, cellSize, cellSize);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(x, y, cellSize, cellSize);

                // Desenhar peça se houver
                Piece piece = board.getPiece(row, col);
                if (piece.getType() != PieceType.EMPTY) {
                    drawPiece(g2d, x, y, piece.getType(), cellSize);
                }
            }
        }

        // Destacar célula selecionada
        if (selectedRow != -1) {
            g2d.setColor(Color.YELLOW);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(xOffset + selectedCol * cellSize,
                    yOffset + selectedRow * cellSize,
                    cellSize, cellSize);
        }

        // Destacar borda do tabuleiro quando for sua vez
        if (isMyTurn) {
            g2d.setColor(Color.GREEN);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRect(xOffset - 2, yOffset - 2,
                    cellSize * Board.getBoardSize() + 4,
                    cellSize * Board.getBoardSize() + 4);
        }
    }

    private void drawPiece(Graphics2D g2d, int x, int y, PieceType type, int cellSize) {
        int padding = cellSize / 10;
        int pieceSize = cellSize - (2 * padding);

        if (type == PieceType.PLAYER1) {
            g2d.setColor(Color.BLACK);
            g2d.fillOval(x + padding, y + padding, pieceSize, pieceSize);
        } else if (type == PieceType.PLAYER2) {
            g2d.setColor(Color.RED);
            g2d.fillOval(x + padding, y + padding, pieceSize, pieceSize);
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(x + padding, y + padding, pieceSize, pieceSize);
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


