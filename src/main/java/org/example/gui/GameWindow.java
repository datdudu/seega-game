package org.example.gui;

import javax.swing.*;
import java.awt.*;

public class GameWindow extends JFrame {
    private BoardPanel boardPanel;
    private ChatPanel chatPanel;
    private LogPanel logPanel;  // Novo componente
    private JButton surrenderButton;
    private JButton closeButton;
    private JLabel statusLabel;
    private Runnable onCloseHandler;

    public GameWindow() {
        setTitle("Seega Game");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Inicializa componentes
        boardPanel = new BoardPanel();
        chatPanel = new ChatPanel();
        logPanel = new LogPanel();  // Inicializa o log
        surrenderButton = new JButton("Desistir");
        closeButton = new JButton("Fechar Jogo");
        statusLabel = new JLabel("Aguardando conexão...", SwingConstants.CENTER);

        // Layout
        JPanel gamePanel = new JPanel(new BorderLayout(10, 10));
        gamePanel.add(boardPanel, BorderLayout.CENTER);

        // Painel lateral direito com chat e log
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.add(chatPanel, BorderLayout.CENTER);
        rightPanel.add(logPanel, BorderLayout.SOUTH);  // Adiciona log abaixo do chat
        gamePanel.add(rightPanel, BorderLayout.EAST);

        // Painel de botões
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(surrenderButton);
        bottomPanel.add(closeButton);

        // Adiciona componentes
        add(statusLabel, BorderLayout.NORTH);
        add(gamePanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Configuração do botão de fechar
        closeButton.addActionListener(e -> {
            if (onCloseHandler != null) {
                onCloseHandler.run();
            }
        });

        // Configurações da janela
        pack();
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 700));  // Aumentado para acomodar o log
    }

    // Getter para o LogPanel
    public LogPanel getLogPanel() {
        return logPanel;
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Erro",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public void setOnCloseHandler(Runnable handler) {
        this.onCloseHandler = handler;

        // Adiciona um listener para o evento de fechamento da janela
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (onCloseHandler != null) {
                    onCloseHandler.run();
                }
            }
        });
    }

    public BoardPanel getBoardPanel() {
        return boardPanel;
    }

    public ChatPanel getChatPanel() {
        return chatPanel;
    }

    public void setSurrenderListener(Runnable listener) {
        surrenderButton.addActionListener(e -> listener.run());
    }

    public void updateStatus(String status) {
        statusLabel.setText(status);
    }

    public void showGameOver(String message) {
        JOptionPane.showMessageDialog(this,
                message,
                "Fim de Jogo",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
