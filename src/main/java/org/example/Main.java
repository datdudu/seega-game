package org.example;

import org.example.controller.GameController;
import org.example.gui.LobbyWindow;
import org.example.network.MainServer;

public class Main {
    public static void main(String[] args) {
        LobbyWindow lobby = new LobbyWindow();

        lobby.setOnHostSelected(() -> {
            // Inicia o servidor com o tipo de comunicação selecionado
            new Thread(() -> {
                try {
                    String[] serverArgs = {
                            String.valueOf(lobby.getSelectedPort()),
                            lobby.getSelectedCommunicationType().toString()
                    };
                    MainServer.main(serverArgs);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            // Pequena pausa para garantir que o servidor iniciou
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Conecta como host com o tipo de comunicação selecionado
            GameController controller = new GameController(lobby.getSelectedCommunicationType());
            controller.connectToServer(lobby.getSelectedIP(), lobby.getSelectedPort());
        });

        lobby.setOnJoinSelected(() -> {
            // Conecta como cliente com o tipo de comunicação selecionado
            GameController controller = new GameController(lobby.getSelectedCommunicationType());
            controller.connectToServer(lobby.getSelectedIP(), lobby.getSelectedPort());
        });

        lobby.setVisible(true);
    }
}