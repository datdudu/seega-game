package org.example.gui;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Painel de log para exibir eventos do jogo com timestamp.
 */
public class LogPanel extends JPanel {
    private JTextArea logArea;           // Área de texto para exibir logs
    private SimpleDateFormat timeFormat; // Formato de hora para timestamp

    /**
     * Construtor: inicializa componentes e layout.
     */
    public LogPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Log de Eventos"));

        timeFormat = new SimpleDateFormat("HH:mm:ss");

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(200, 150));

        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Adiciona uma mensagem ao log com timestamp.
     */
    public void addLog(String message) {
        String timestamp = timeFormat.format(new Date());
        SwingUtilities.invokeLater(() -> {
            logArea.append(String.format("[%s] %s%n", timestamp, message));
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    /**
     * Limpa o log.
     */
    public void clear() {
        logArea.setText("");
    }
}