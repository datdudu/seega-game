package org.example.controller;

import org.example.common.CommunicationType;
import org.example.common.PieceType;
import org.example.communication.*;
import org.example.gui.BoardPanel;
import org.example.gui.ChatPanel;
import org.example.gui.GameWindow;
import org.example.model.Board;
import org.example.model.Piece;

import javax.swing.*;
import java.util.List;

/**
 * Controlador principal do jogo Seega.
 * Gerencia a lógica do jogo, interação com a interface gráfica e comunicação em rede.
 * Implementa os listeners para eventos de comunicação e chat.
 */
public class GameController implements GameCommunicationListener, ChatPanel.ChatListener {
    private GameWindow gameWindow;         // Janela principal do jogo
    private Board board;                   // Modelo do tabuleiro
    private GameCommunication communication; // Comunicação (socket, RPC, etc)
    private boolean isMyTurn;              // Indica se é a vez do jogador local
    private PieceType myPieceType;         // Tipo da peça do jogador local
    private boolean gameStarted;           // Indica se o jogo já começou

    /**
     * Construtor: inicializa o controlador, a interface e a comunicação.
     */
    public GameController(CommunicationType communicationType) {
        this.board = new Board();
        this.gameWindow = new GameWindow();
        this.communication = CommunicationFactory.createCommunication(communicationType);
        this.communication.setGameCommunicationListener(this);
        setupGameWindow();
    }

    /**
     * Configura os listeners da interface gráfica.
     */
    private void setupGameWindow() {
        // Listener do tabuleiro (movimento e colocação de peças)
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

        // Listener do chat
        gameWindow.getChatPanel().setChatListener(this);

        // Listener do botão de desistir
        gameWindow.setSurrenderListener(this::handleSurrender);

        // Handler para fechamento da janela
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

    /**
     * Conecta ao servidor usando o tipo de comunicação escolhido.
     */
    public void connectToServer(String host, int port) {
        try {
            communication.connect(host, port);
        } catch (CommunicationException e) {
            showError("Erro ao conectar: " + e.getMessage());
        }
    }

    /**
     * Adiciona uma mensagem ao log de eventos.
     */
    private void logEvent(String message) {
        gameWindow.getLogPanel().addLog(message);
    }

    /**
     * Realiza um movimento no tabuleiro e envia para o oponente.
     */
    private void makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (board.isValidMove(fromRow, fromCol, toRow, toCol, myPieceType)) {
            board.movePiece(fromRow, fromCol, toRow, toCol);
            communication.sendMove(fromRow, fromCol, toRow, toCol);

            logEvent(String.format("Movimento enviado: (%d,%d) -> (%d,%d)",
                    fromRow, fromCol, toRow, toCol));

            // Verifica e remove peças capturadas
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

    /**
     * Coloca uma peça no tabuleiro durante a fase de preparação.
     */
    private void placePiece(int row, int col) {
        if (board.placePiece(row, col, myPieceType)) {
            communication.sendMove(-1, -1, row, col);
            logEvent(String.format("Peça colocada em (%d,%d) - Restam %d peças neste turno",
                    row, col, board.getPiecesRemainingThisTurn()));

            gameWindow.getBoardPanel().updateBoard(board);

            if (board.shouldChangeTurn()) {
                isMyTurn = false;
                board.resetTurnCounter();
                logEvent("Turno finalizado - colocou 2 peças");
            }

            updateGameState();
        }
    }

    /**
     * Recebe e processa um movimento do oponente.
     */
    @Override
    public void onMoveReceived(String moveData) {
        String[] parts = moveData.split(",");
        int fromRow = Integer.parseInt(parts[0]);
        int fromCol = Integer.parseInt(parts[1]);
        int toRow = Integer.parseInt(parts[2]);
        int toCol = Integer.parseInt(parts[3]);

        if (fromRow == -1 && fromCol == -1) {
            // Fase de preparação: oponente colocou peça
            board.placePiece(toRow, toCol, getOpponentPieceType());
            logEvent(String.format("Oponente colocou peça em (%d,%d)", toRow, toCol));

            gameWindow.getBoardPanel().updateBoard(board);

            if (board.shouldChangeTurn()) {
                isMyTurn = true;
                board.resetTurnCounter();
                logEvent("Sua vez - coloque 2 peças");
            }
        } else {
            // Fase de movimento: oponente moveu peça
            board.movePiece(fromRow, fromCol, toRow, toCol);
            gameWindow.getBoardPanel().updateBoard(board);

            logEvent(String.format("Movimento do oponente: (%d,%d) -> (%d,%d)",
                    fromRow, fromCol, toRow, toCol));

            // Verifica e remove peças capturadas pelo oponente
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

    /**
     * Evento disparado quando o jogo começa.
     */
    @Override
    public void onGameStart(boolean isFirstPlayer) {
        gameStarted = true;
        myPieceType = isFirstPlayer ? PieceType.PLAYER1 : PieceType.PLAYER2;
        isMyTurn = isFirstPlayer;
        logEvent("Jogo iniciado - " + (isFirstPlayer ? "Primeiro" : "Segundo") + " jogador");
        updateGameState();
    }

    /**
     * Evento disparado ao receber mensagem de chat.
     */
    @Override
    public void onChatReceived(String message) {
        gameWindow.getChatPanel().addMessage("Oponente: " + message);
        logEvent("Mensagem recebida do oponente: " + message);
    }

    /**
     * Handler para o botão de desistir.
     */
    private void handleSurrender() {
        if (communication != null) {
            gameWindow.showGameOver("Você desistiu, o seu oponente é o vencedor!");
            logEvent("Você desistiu da partida");
            communication.surrender();
            new Timer(2000, e -> shutdown()).start();
        }
    }

    /**
     * Evento disparado quando o jogo termina.
     */
    @Override
    public void onGameEnd(String reason) {
        logEvent("Fim de jogo: " + reason);
        SwingUtilities.invokeLater(() -> {
            gameWindow.showGameOver(reason);
            new Timer(2000, e -> shutdown()).start();
        });
    }

    /**
     * Evento disparado em caso de erro de comunicação.
     */
    @Override
    public void onError(String error) {
        logEvent("ERRO: " + error);
        SwingUtilities.invokeLater(() -> {
            gameWindow.showError(error);
            new Timer(2000, e -> shutdown()).start();
        });
    }

    /**
     * Evento disparado ao enviar mensagem de chat.
     */
    @Override
    public void onMessageSent(String message) {
        if (communication != null) {
            communication.sendChat(message);
            gameWindow.getChatPanel().addMessage("Você: " + message);
            logEvent("Mensagem enviada: " + message);
        }
    }

    /**
     * Verifica condições de vitória após cada jogada.
     */
    private void checkWinCondition() {
        PieceType opponent = getOpponentPieceType();
        boolean opponentHasPieces = false;
        boolean opponentHasMoves = false;

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
            if (communication != null) {
                communication.sendEndGame("VICTORY_CAPTURED_ALL");
            }
            gameWindow.showGameOver("Você venceu! Capturou todas as peças do oponente!");
            new Timer(2000, e -> shutdown()).start();
        } else if (!opponentHasMoves) {
            if (communication != null) {
                communication.sendEndGame("VICTORY_NO_MOVES");
            }
            gameWindow.showGameOver("Você venceu! Oponente sem movimentos válidos!");
            new Timer(2000, e -> shutdown()).start();
        }
    }

    /**
     * Atualiza o estado visual do jogo (tabuleiro, status, etc).
     */
    private void updateGameState() {
        gameWindow.getBoardPanel().setMyTurn(isMyTurn);
        gameWindow.getBoardPanel().setCurrentPlayer(myPieceType);
        gameWindow.getBoardPanel().repaint();

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

    /**
     * Retorna o tipo de peça do oponente.
     */
    private PieceType getOpponentPieceType() {
        return (myPieceType == PieceType.PLAYER1) ?
                PieceType.PLAYER2 : PieceType.PLAYER1;
    }

    /**
     * Encerra o jogo, desconectando e fechando a janela.
     */
    public void shutdown() {
        if (communication != null) {
            communication.disconnect();
        }
        if (gameWindow != null) {
            gameWindow.dispose();
        }
        System.exit(0);
    }

    /**
     * Exibe mensagem de erro na interface.
     */
    private void showError(String message) {
        SwingUtilities.invokeLater(() -> {
            gameWindow.showError(message);
        });
    }
}