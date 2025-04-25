package org.example.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Painel de chat para troca de mensagens entre os jogadores.
 */
public class ChatPanel extends JPanel {
    private JTextArea chatArea;         // Área de exibição das mensagens
    private JTextField messageField;    // Campo de digitação
    private JButton sendButton;         // Botão de envio
    private ChatListener chatListener;  // Listener para envio de mensagens

    /**
     * Interface para notificar o controlador quando uma mensagem é enviada.
     */
    public interface ChatListener {
        void onMessageSent(String message);
    }

    /**
     * Construtor: inicializa componentes e layout.
     */
    public ChatPanel() {
        setPreferredSize(new Dimension(200, 400));
        setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        messageField = new JTextField();
        sendButton = new JButton("Enviar");

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setupListeners();
    }

    /**
     * Configura listeners para envio de mensagens.
     */
    private void setupListeners() {
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());
    }

    /**
     * Envia a mensagem digitada, se não estiver vazia.
     */
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty() && chatListener != null) {
            chatListener.onMessageSent(message);
            messageField.setText("");
        }
    }

    /**
     * Adiciona uma mensagem ao chat (exibição para o usuário).
     */
    public void addMessage(String message) {
        chatArea.append(message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    /**
     * Define o listener para envio de mensagens.
     */
    public void setChatListener(ChatListener listener) {
        this.chatListener = listener;
    }
}