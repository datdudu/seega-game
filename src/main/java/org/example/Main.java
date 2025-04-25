package org.example;

import org.example.controller.GameController;
import org.example.gui.LobbyWindow;
import org.example.network.MainServer;

public class Main {
    public static void main(String[] args) {
        // Cria a janela de lobby, que será o ponto de entrada do jogo
        // Aqui o usuário escolherá se quer hospedar ou conectar em uma partida
        LobbyWindow lobby = new LobbyWindow();

        // Configura o comportamento quando o usuário escolhe hospedar uma partida
        lobby.setOnHostSelected(() -> {
            // Inicia o servidor em uma thread separada para não bloquear a interface
            new Thread(() -> {
                try {
                    // Prepara os argumentos para o servidor:
                    // - A porta selecionada no lobby
                    // - O tipo de comunicação escolhido (Socket/RPC)
                    String[] serverArgs = {
                            String.valueOf(lobby.getSelectedPort()),
                            lobby.getSelectedCommunicationType().toString()
                    };
                    // Inicia o servidor com os argumentos configurados
                    MainServer.main(serverArgs);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            // Aguarda 1 segundo para garantir que o servidor iniciou
            // antes de tentar conectar o cliente
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Cria o controlador do jogo para o host com o tipo de comunicação selecionado
            // O host também é um cliente, então precisa se conectar ao servidor
            GameController controller = new GameController(lobby.getSelectedCommunicationType());
            controller.connectToServer(lobby.getSelectedIP(), lobby.getSelectedPort());
        });

        // Configura o comportamento quando o usuário escolhe conectar em uma partida
        lobby.setOnJoinSelected(() -> {
            // Cria o controlador do jogo para o cliente
            // Não precisa iniciar servidor, apenas conecta ao existente
            GameController controller = new GameController(lobby.getSelectedCommunicationType());
            controller.connectToServer(lobby.getSelectedIP(), lobby.getSelectedPort());
        });

        // Exibe a janela de lobby e aguarda a escolha do usuário
        lobby.setVisible(true);
    }
}

