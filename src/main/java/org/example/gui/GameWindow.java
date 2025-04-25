package org.example.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Janela principal do jogo Seega.
 * Contém o tabuleiro, chat, log de eventos, botões de controle e status.
 */
public class GameWindow extends JFrame {
    private BoardPanel boardPanel;     // Painel do tabuleiro
    private ChatPanel chatPanel;       // Painel de chat
    private LogPanel logPanel;         // Painel de log de eventos
    private JButton surrenderButton;   // Botão para desistir
    private JButton closeButton;       // Botão para fechar o jogo
    private JLabel statusLabel;        // Label de status (ex: "Aguardando conexão...")
    private Runnable onCloseHandler;   // Handler para fechamento customizado

    /**
     * Construtor: inicializa e organiza todos os componentes da janela.
     */
    public GameWindow() {
        setTitle("Seega Game");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Inicializa componentes principais
        boardPanel = new BoardPanel();
        chatPanel = new ChatPanel();
        logPanel = new LogPanel();
        surrenderButton = new JButton("Desistir");
        closeButton = new JButton("Fechar Jogo");
        statusLabel = new JLabel("Aguardando conexão...", SwingConstants.CENTER);

        // Painel central com o tabuleiro
        JPanel gamePanel = new JPanel(new BorderLayout(10, 10));
        gamePanel.add(boardPanel, BorderLayout.CENTER);

        // Painel lateral direito com chat e log
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.add(chatPanel, BorderLayout.CENTER);
        rightPanel.add(logPanel, BorderLayout.SOUTH);
        gamePanel.add(rightPanel, BorderLayout.EAST);

        // Painel inferior com botões
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(surrenderButton);
        bottomPanel.add(closeButton);

        // Adiciona componentes à janela
        add(statusLabel, BorderLayout.NORTH);
        add(gamePanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Handler do botão de fechar
        closeButton.addActionListener(e -> {
            if (onCloseHandler != null) {
                onCloseHandler.run();
            }
        });

        // Configuração da janela
        pack();
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 700));
    }

    // Getters para acesso aos painéis e botões
    public LogPanel getLogPanel() { return logPanel; }
    public BoardPanel getBoardPanel() { return boardPanel; }
    public ChatPanel getChatPanel() { return chatPanel; }

    // Exibe mensagem de erro em popup
    public void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Erro",
                JOptionPane.ERROR_MESSAGE
        );
    }

    // Define handler customizado para fechamento da janela
    public void setOnCloseHandler(Runnable handler) {
        this.onCloseHandler = handler;
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (onCloseHandler != null) {
                    onCloseHandler.run();
                }
            }
        });
    }

    // Define listener para o botão de desistir
    public void setSurrenderListener(Runnable listener) {
        surrenderButton.addActionListener(e -> listener.run());
    }

    // Atualiza o texto do status
    public void updateStatus(String status) {
        statusLabel.setText(status);
    }

    // Exibe mensagem de fim de jogo em popup
    public void showGameOver(String message) {
        JOptionPane.showMessageDialog(this,
                message,
                "Fim de Jogo",
                JOptionPane.INFORMATION_MESSAGE);
    }
}