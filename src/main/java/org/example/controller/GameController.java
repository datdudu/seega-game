package org.example.controller;


import org.example.common.PieceType;
import org.example.gui.BoardPanel;
import org.example.gui.ChatPanel;
import org.example.gui.GameWindow;
import org.example.model.Board;
import org.example.model.Piece;
import org.example.network.GameClient;

import javax.swing.*;
import java.util.List;

public class GameController implements GameClient.GameClientListener, ChatPanel.ChatListener {
    private GameWindow gameWindow;
    private Board board;
    private GameClient client;
    private boolean isMyTurn;
    private PieceType myPieceType;
    private boolean gameStarted;

    public GameController() {
        this.board = new Board();
        this.gameWindow = new GameWindow();
        setupGameWindow();
    }

    private void setupGameWindow() {
        // Configura o listener do tabuleiro
        gameWindow.getBoardPanel().setBoardClickListener(new BoardPanel.BoardClickListener() {
            @Override
            public void onMove(int fromRow, int fromCol, int toRow, int toCol) {
                if (isMyTurn) {
                    makeMove(fromRow, fromCol, toRow, toCol);
                }
            }

            @Override
            public void onPiecePlacement(int row, int col) {
                if (isMyTurn && board.isSetupPhase()) {
                    placePiece(row, col);
                }
            }
        });

        // Configura o chat
        gameWindow.getChatPanel().setChatListener(this);

        // Configura o botão de desistência
        gameWindow.setSurrenderListener(() -> {
            if (client != null) {
                client.surrender();
            }
        });

        // Configura o handler de fechamento
        gameWindow.setOnCloseHandler(() -> {
            int confirm = JOptionPane.showConfirmDialog(
                    gameWindow,
                    "Tem certeza que deseja sair do jogo?",
                    "Confirmar Saída",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                shutdown();
            }
        });

        gameWindow.setVisible(true);
    }

    public void connectToServer(String host, int port) {
        try {
            client = new GameClient(host, port, this);
        } catch (Exception e) {
            showError("Erro ao conectar: " + e.getMessage());
        }
    }

    private void logEvent(String message) {
        gameWindow.getLogPanel().addLog(message);
    }

    private void makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (board.isValidMove(fromRow, fromCol, toRow, toCol, myPieceType)) {
            board.movePiece(fromRow, fromCol, toRow, toCol);
            client.sendMove(fromRow, fromCol, toRow, toCol);

            logEvent(String.format("Movimento enviado: (%d,%d) -> (%d,%d)",
                    fromRow, fromCol, toRow, toCol));

            List<Piece> capturedPieces = board.checkCaptures(toRow, toCol, myPieceType);
            for (Piece piece : capturedPieces) {
                board.removePiece(piece.getRow(), piece.getCol());
                logEvent(String.format("Peça capturada em (%d,%d)",
                        piece.getRow(), piece.getCol()));
            }

            isMyTurn = false;
            updateGameState();
            checkWinCondition();
        }
    }

    private void placePiece(int row, int col) {
        if (board.placePiece(row, col, myPieceType)) {
            client.sendMove(-1, -1, row, col);
            logEvent(String.format("Peça colocada em (%d,%d) - Restam %d peças neste turno",
                    row, col, board.getPiecesRemainingThisTurn()));

            // Força atualização visual
            gameWindow.getBoardPanel().updateBoard(board);

            // Verifica se deve mudar o turno
            if (board.shouldChangeTurn()) {
                isMyTurn = false;
                board.resetTurnCounter();
                logEvent("Turno finalizado - colocou 2 peças");
            }

            updateGameState();
        }
    }

    @Override
    public void onMoveReceived(String moveData) {
        String[] parts = moveData.split(",");
        int fromRow = Integer.parseInt(parts[0]);
        int fromCol = Integer.parseInt(parts[1]);
        int toRow = Integer.parseInt(parts[2]);
        int toCol = Integer.parseInt(parts[3]);

        if (fromRow == -1 && fromCol == -1) {
            // Colocação de peça na fase de preparação
            board.placePiece(toRow, toCol, getOpponentPieceType());
            logEvent(String.format("Oponente colocou peça em (%d,%d)", toRow, toCol));

            // Força atualização visual
            gameWindow.getBoardPanel().updateBoard(board);

            // Se o oponente colocou duas peças, é sua vez
            if (board.shouldChangeTurn()) {
                isMyTurn = true;
                board.resetTurnCounter();
                logEvent("Sua vez - coloque 2 peças");
            }
        } else {
            // Movimento normal
            board.movePiece(fromRow, fromCol, toRow, toCol);

            // Força atualização visual
            gameWindow.getBoardPanel().updateBoard(board);

            logEvent(String.format("Movimento do oponente: (%d,%d) -> (%d,%d)",
                    fromRow, fromCol, toRow, toCol));

            List<Piece> capturedPieces = board.checkCaptures(toRow, toCol, getOpponentPieceType());
            for (Piece piece : capturedPieces) {
                board.removePiece(piece.getRow(), piece.getCol());
                logEvent(String.format("Peça capturada em (%d,%d)",
                        piece.getRow(), piece.getCol()));
            }
            isMyTurn = true;
        }

        updateGameState();
    }


    @Override
    public void onGameStart(boolean isFirstPlayer) {
        gameStarted = true;
        myPieceType = isFirstPlayer ? PieceType.PLAYER1 : PieceType.PLAYER2;
        isMyTurn = isFirstPlayer;
        logEvent("Jogo iniciado - " + (isFirstPlayer ? "Primeiro" : "Segundo") + " jogador");
        updateGameState();
    }


    @Override
    public void onChatReceived(String message) {
        gameWindow.getChatPanel().addMessage("Oponente: " + message);
        logEvent("Mensagem recebida do oponente: " + message);
    }

    @Override
    public void onGameEnd(String reason) {
        logEvent("Fim de jogo: " + reason);
        SwingUtilities.invokeLater(() -> {
            gameWindow.showGameOver(reason);
            new Timer(2000, e -> shutdown()).start();
        });
    }

    @Override
    public void onError(String error) {
        logEvent("ERRO: " + error);
        SwingUtilities.invokeLater(() -> {
            gameWindow.showError(error);
            new Timer(2000, e -> shutdown()).start();
        });
    }

    @Override
    public void onMessageSent(String message) {
        if (client != null) {
            client.sendChat(message);
            gameWindow.getChatPanel().addMessage("Você: " + message);
            logEvent("Mensagem enviada: " + message);
        }
    }

    private void checkWinCondition() {
        PieceType opponent = getOpponentPieceType();
        boolean opponentHasPieces = false;
        boolean opponentHasMoves = false;

        // Verifica se o oponente ainda tem peças e movimentos válidos
        for (int row = 0; row < Board.getBoardSize(); row++) {
            for (int col = 0; col < Board.getBoardSize(); col++) {
                if (board.getPiece(row, col).getType() == opponent) {
                    opponentHasPieces = true;
                    if (board.hasValidMoves(opponent)) {
                        opponentHasMoves = true;
                        break;
                    }
                }
            }
        }

        if (!opponentHasPieces) {
            gameWindow.showGameOver("Você venceu! Capturou todas as peças do oponente!");
            shutdown();
        } else if (!opponentHasMoves) {
            gameWindow.showGameOver("Você venceu! Oponente sem movimentos válidos!");
            shutdown();
        }
    }


    private void updateGameState() {
        gameWindow.getBoardPanel().setMyTurn(isMyTurn);
        gameWindow.getBoardPanel().setCurrentPlayer(myPieceType);
        gameWindow.getBoardPanel().repaint(); // Força atualização visual

        String status;
        if (board.isSetupPhase()) {
            if (isMyTurn) {
                status = String.format("Fase de preparação - Coloque %d peça(s)",
                        board.getPiecesRemainingThisTurn());
            } else {
                status = "Fase de preparação - Aguardando oponente";
            }
        } else {
            status = isMyTurn ? "Sua vez" : "Aguardando oponente...";
        }
        gameWindow.updateStatus(status);
    }

    private PieceType getOpponentPieceType() {
        return (myPieceType == PieceType.PLAYER1) ?
                PieceType.PLAYER2 : PieceType.PLAYER1;
    }

    public void shutdown() {
        // Fecha o cliente de rede
        if (client != null) {
            client.close();
        }

        // Fecha a janela
        if (gameWindow != null) {
            gameWindow.dispose();
        }

        // Encerra a aplicação
        System.exit(0);
    }

    private void showError(String message) {
        SwingUtilities.invokeLater(() -> {
            gameWindow.showError(message);
        });
    }
}