package org.example;

// SeegaGame.java
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class SeegaGame extends JFrame {
    private SeegaBoard board;
    private ChatGUI chatPanel;
    private String playerName;
    private boolean isFirstPlayer;
    private JButton forfeitButton;
    private JButton passTurnButton;
    private JButton toggleChatButton;
    private JPanel chatContainer;
    private CommunicationInterface communication;

    public SeegaGame(String playerName, boolean isFirstPlayer) {
        this.playerName = playerName;
        this.isFirstPlayer = isFirstPlayer;

        setTitle("Seega - " + playerName);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initializeComponents();
        setupLayout();

        pack();
        setLocationRelativeTo(null);
    }

    private void initializeComponents() {
        // Inicializar o tabuleiro
        board = new SeegaBoard(playerName, "Oponente", new SeegaBoard.GameCallback() {
            @Override
            public void onMoveMade(int fromRow, int fromCol, int toRow, int toCol) {
                try {
                    communication.sendMessage("MOVE:" + fromRow + "," + fromCol + "," + toRow + "," + toCol);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onGameEnd(String winner) {
                JOptionPane.showMessageDialog(SeegaGame.this,
                        "Jogo terminado! Vencedor: " + winner);
            }
        });

        // Inicializar chat
        chatPanel = new ChatGUI(playerName);
        chatContainer = new JPanel(new BorderLayout());
        chatContainer.add(chatPanel, BorderLayout.CENTER);
        chatContainer.setPreferredSize(new Dimension(200, 0));
        chatContainer.setVisible(false);

        // Inicializar botões
        forfeitButton = new JButton("Desistir");
        passTurnButton = new JButton("Passar vez");
        toggleChatButton = new JButton("Chat");

        // Configurar ações dos botões
        setupButtonActions();
    }

    private void setupButtonActions() {
        forfeitButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Tem certeza que deseja desistir?",
                    "Confirmar desistência",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                board.forfeitGame();
            }
        });

        passTurnButton.addActionListener(e -> {
            board.passTurn();
            try {
                communication.sendMessage("PASS_TURN");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        toggleChatButton.addActionListener(e -> {
            chatContainer.setVisible(!chatContainer.isVisible());
            pack();
        });
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        // Painel do jogo (esquerda)
        JPanel gamePanel = new JPanel(new BorderLayout(5, 5));
        gamePanel.add(board, BorderLayout.CENTER);

        // Painel de botões
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(forfeitButton);
        buttonPanel.add(passTurnButton);
        buttonPanel.add(toggleChatButton);
        gamePanel.add(buttonPanel, BorderLayout.SOUTH);

        // Adicionar painéis principais
        add(gamePanel, BorderLayout.CENTER);
        add(chatContainer, BorderLayout.EAST);

        // Adicionar padding
        ((JPanel)getContentPane()).setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    public void start() {
        setVisible(true);
        chatPanel.start();
    }

    public static void main(String[] args) {
        String playerName = JOptionPane.showInputDialog(null,
                "Digite seu nome:",
                "Bem-vindo ao Seega",
                JOptionPane.QUESTION_MESSAGE);

        if (playerName != null && !playerName.trim().isEmpty()) {
            SwingUtilities.invokeLater(() -> {
                SeegaGame game = new SeegaGame(playerName, true);
                game.start();
            });
        }
    }
}

