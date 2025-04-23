package org.example.gui;

import org.example.common.CommunicationType;

import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;

public class LobbyWindow extends JFrame {
    private Runnable onHostSelected;
    private Runnable onJoinSelected;
    private String selectedIP;
    private int selectedPort;
    private CommunicationType selectedCommunicationType;
    private JComboBox<String> communicationTypeCombo;

    public LobbyWindow() {
        setTitle("Seega - Lobby");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setResizable(false);

        // Painel principal com layout de grade
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Título
        JLabel titleLabel = new JLabel("Bem-vindo ao Seega!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(titleLabel, gbc);

        // Painel de seleção de comunicação
        JPanel communicationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel communicationLabel = new JLabel("Tipo de Comunicação:");
        communicationTypeCombo = new JComboBox<>(new String[]{"Socket", "RPC"});
        communicationPanel.add(communicationLabel);
        communicationPanel.add(communicationTypeCombo);

        gbc.gridy = 1;
        gbc.gridwidth = 2;
        mainPanel.add(communicationPanel, gbc);

        // Botão Hospedar
        JButton hostButton = new JButton("Hospedar Partida");
        hostButton.setPreferredSize(new Dimension(200, 40));
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        mainPanel.add(hostButton, gbc);

        // Botão Conectar
        JButton joinButton = new JButton("Conectar a uma Partida");
        joinButton.setPreferredSize(new Dimension(200, 40));
        gbc.gridy = 3;
        mainPanel.add(joinButton, gbc);

        // Adiciona o painel principal
        add(mainPanel, BorderLayout.CENTER);

        // Handler do ComboBox
        communicationTypeCombo.addActionListener(e -> {
            String selected = (String) communicationTypeCombo.getSelectedItem();
            if ("RPC".equals(selected)) {
                JOptionPane.showMessageDialog(this,
                        "Comunicação RPC ainda não implementada!",
                        "Aviso",
                        JOptionPane.WARNING_MESSAGE);
                communicationTypeCombo.setSelectedItem("Socket");
            }
            updateSelectedCommunicationType();
        });

        // Handlers dos botões
        hostButton.addActionListener(e -> showHostDialog());
        joinButton.addActionListener(e -> showJoinDialog());

        // Configuração inicial do tipo de comunicação
        updateSelectedCommunicationType();

        // Configurações finais da janela
        pack();
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(400, 300));
    }

    private void updateSelectedCommunicationType() {
        String selected = (String) communicationTypeCombo.getSelectedItem();
        switch (selected) {
            case "Socket":
                selectedCommunicationType = CommunicationType.SOCKET;
                break;
            case "RPC":
                selectedCommunicationType = CommunicationType.RPC;
                break;
        }
    }

    private void showHostDialog() {
        try {
            String localIP = InetAddress.getLocalHost().getHostAddress();

            // Cria um painel para a porta
            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.add(new JLabel("Seu IP: " + localIP));
            panel.add(new JLabel("Digite a porta para hospedar:"));
            JTextField portField = new JTextField("12345");
            panel.add(portField);

            int result = JOptionPane.showConfirmDialog(this, panel,
                    "Hospedar Partida", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    selectedPort = Integer.parseInt(portField.getText().trim());
                    selectedIP = localIP;
                    if (onHostSelected != null) {
                        setVisible(false);
                        onHostSelected.run();
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Porta inválida!", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao obter IP local: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showJoinDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("IP do Host:"));
        JTextField ipField = new JTextField("localhost");
        panel.add(ipField);
        panel.add(new JLabel("Porta:"));
        JTextField portField = new JTextField("12345");
        panel.add(portField);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Conectar a uma Partida", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                selectedIP = ipField.getText().trim();
                selectedPort = Integer.parseInt(portField.getText().trim());
                if (onJoinSelected != null) {
                    setVisible(false);
                    onJoinSelected.run();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Porta inválida!", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void setOnHostSelected(Runnable callback) {
        this.onHostSelected = callback;
    }

    public void setOnJoinSelected(Runnable callback) {
        this.onJoinSelected = callback;
    }

    public String getSelectedIP() {
        return selectedIP;
    }

    public int getSelectedPort() {
        return selectedPort;
    }

    public CommunicationType getSelectedCommunicationType() {
        return selectedCommunicationType;
    }
}