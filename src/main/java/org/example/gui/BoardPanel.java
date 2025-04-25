package org.example.gui;

import org.example.common.PieceType;
import org.example.model.Board;
import org.example.model.Piece;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Painel gráfico responsável por desenhar o tabuleiro do Seega e gerenciar interações do usuário.
 */
public class BoardPanel extends JPanel {
    private Board board; // Estado atual do tabuleiro
    private int selectedRow = -1; // Linha da peça selecionada para mover
    private int selectedCol = -1; // Coluna da peça selecionada para mover
    private BoardClickListener clickListener; // Listener para eventos de clique
    private PieceType currentPlayer; // Jogador atual (para destacar peças)
    private boolean isMyTurn; // Indica se é a vez do usuário

    /**
     * Interface para notificar o controlador sobre cliques no tabuleiro.
     */
    public interface BoardClickListener {
        void onMove(int fromRow, int fromCol, int toRow, int toCol);
        void onPiecePlacement(int row, int col);
    }

    /**
     * Construtor: inicializa o painel e configura o mouse listener.
     */
    public BoardPanel() {
        board = new Board();
        currentPlayer = PieceType.EMPTY;
        isMyTurn = false;

        // Define tamanho mínimo e preferido do painel
        setMinimumSize(new Dimension(400, 400));
        setPreferredSize(new Dimension(400, 400));

        // Listener de clique do mouse
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isMyTurn) return; // Ignora cliques se não for a vez do usuário

                // Calcula tamanho das células e offsets para centralizar o tabuleiro
                int size = Math.min(getWidth(), getHeight());
                int cellSize = size / Board.getBoardSize();
                int xOffset = (getWidth() - (cellSize * Board.getBoardSize())) / 2;
                int yOffset = (getHeight() - (cellSize * Board.getBoardSize())) / 2;

                // Determina a célula clicada
                int row = (e.getY() - yOffset) / cellSize;
                int col = (e.getX() - xOffset) / cellSize;

                // Verifica se o clique foi dentro do tabuleiro
                if (row >= 0 && row < Board.getBoardSize() &&
                        col >= 0 && col < Board.getBoardSize()) {
                    handleClick(row, col);
                }
            }
        });
    }

    /**
     * Desenha o tabuleiro, peças e destaques.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Melhora a qualidade visual
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int size = Math.min(getWidth(), getHeight());
        int cellSize = size / Board.getBoardSize();
        int xOffset = (getWidth() - (cellSize * Board.getBoardSize())) / 2;
        int yOffset = (getHeight() - (cellSize * Board.getBoardSize())) / 2;

        // Fundo do tabuleiro
        g2d.setColor(new Color(240, 240, 240));
        g2d.fillRect(xOffset, yOffset, cellSize * Board.getBoardSize(),
                cellSize * Board.getBoardSize());

        // Grade e peças
        for (int row = 0; row < Board.getBoardSize(); row++) {
            for (int col = 0; col < Board.getBoardSize(); col++) {
                int x = xOffset + (col * cellSize);
                int y = yOffset + (row * cellSize);

                // Célula
                g2d.setColor(Color.WHITE);
                g2d.fillRect(x, y, cellSize, cellSize);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(x, y, cellSize, cellSize);

                // Peça
                Piece piece = board.getPiece(row, col);
                if (piece.getType() != PieceType.EMPTY) {
                    drawPiece(g2d, x, y, piece.getType(), cellSize);
                }
            }
        }

        // Destaca célula selecionada
        if (selectedRow != -1) {
            g2d.setColor(Color.YELLOW);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(xOffset + selectedCol * cellSize,
                    yOffset + selectedRow * cellSize,
                    cellSize, cellSize);
        }

        // Destaca borda do tabuleiro se for a vez do usuário
        if (isMyTurn) {
            g2d.setColor(Color.GREEN);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRect(xOffset - 2, yOffset - 2,
                    cellSize * Board.getBoardSize() + 4,
                    cellSize * Board.getBoardSize() + 4);
        }
    }

    /**
     * Desenha uma peça na célula especificada.
     */
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

    /**
     * Atualiza o tabuleiro e repinta o painel.
     */
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

    /**
     * Lógica de clique: seleção e movimentação de peças ou colocação na fase inicial.
     */
    private void handleClick(int row, int col) {
        if (board.isSetupPhase()) {
            if (clickListener != null) {
                clickListener.onPiecePlacement(row, col);
                repaint();
            }
        } else {
            if (selectedRow == -1) {
                // Seleciona peça do jogador atual
                if (board.getPiece(row, col).getType() == currentPlayer) {
                    selectedRow = row;
                    selectedCol = col;
                }
            } else {
                // Tenta mover para célula destino
                if (clickListener != null) {
                    clickListener.onMove(selectedRow, selectedCol, row, col);
                }
                selectedRow = -1;
                selectedCol = -1;
            }
            repaint();
        }
    }
}