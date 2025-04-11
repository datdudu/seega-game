package org.example;// ChatGUI.java
// ChatGUI.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class ChatGUI extends JPanel {
    private CommunicationInterface communication;
    private String username;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;

    public ChatGUI(String username) {
        this.username = username;
        this.communication = new SocketCommunication();

        setupLayout();
    }

    private void setupLayout() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Área de chat
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 12));
        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setPreferredSize(new Dimension(200, 0));

        // Painel de mensagem
        JPanel messagePanel = new JPanel(new BorderLayout(5, 0));
        messageField = new JTextField();
        messageField.setFont(new Font("Arial", Font.PLAIN, 12));
        sendButton = new JButton("Enviar");
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);

        // Adiciona componentes
        add(chatScroll, BorderLayout.CENTER);
        add(messagePanel, BorderLayout.SOUTH);

        // Configura ações
        setupActions();
    }

    private void setupActions() {
        ActionListener sendAction = e -> sendMessage();
        sendButton.addActionListener(sendAction);
        messageField.addActionListener(sendAction);
        messageField.requestFocus();
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            try {
                String fullMessage = username + ": " + message;
                communication.sendMessage(fullMessage);
                appendMessage("[Você] " + message);
                messageField.setText("");
            } catch (IOException e) {
                showError("Erro ao enviar mensagem: " + e.getMessage());
            }
        }
    }

    private void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    public void start() {
        try {
            communication.connect("localhost", 12345);

            new Thread(() -> {
                try {
                    while (true) {
                        String message = communication.receiveMessage();
                        if (message != null) {
                            appendMessage(message);
                        }
                    }
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() ->
                            showError("Conexão perdida: " + e.getMessage()));
                }
            }).start();

        } catch (IOException e) {
            showError("Erro ao conectar ao servidor: " + e.getMessage());
        }
    }
}