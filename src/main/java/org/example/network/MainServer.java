package org.example.network;

import org.example.communication.*;
import org.example.network.NetworkProtocol;
import org.example.network.socket.SocketGameServer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class MainServer {
    private static final int MAX_PLAYERS = 2;
    private static Map<String, PlayerInfo> players = new ConcurrentHashMap<>();
    private static String firstPlayerConnected = null;

    private static class PlayerInfo {
        String id;
        boolean isFirstPlayer;

        PlayerInfo(String id, boolean isFirstPlayer) {
            this.id = id;
            this.isFirstPlayer = isFirstPlayer;
        }
    }

    public static void main(String[] args) {
        try {
            // Cria o servidor usando a factory
            GameServerCommunication server = GameServerFactory.createServer(GameServerFactory.ServerType.SOCKET);

            // Configura o listener do servidor
            server.setServerListener(new GameServerListener() {
                @Override
                public void onPlayerConnected(String playerId) {
                    System.out.println("Jogador conectado: " + playerId);

                    if (players.size() >= MAX_PLAYERS) {
                        // Servidor cheio
                        ((SocketGameServer)server).sendToPlayer(playerId, "ERROR", "Servidor cheio");
                        return;
                    }

                    // Define se é o primeiro jogador
                    boolean isFirstPlayer = (firstPlayerConnected == null);
                    if (isFirstPlayer) {
                        firstPlayerConnected = playerId;
                    }

                    // Registra o jogador
                    players.put(playerId, new PlayerInfo(playerId, isFirstPlayer));

                    // Se é o segundo jogador, inicia o jogo
                    if (players.size() == MAX_PLAYERS) {
                        startGame(server);
                    }
                }

                @Override
                public void onPlayerDisconnected(String playerId) {
                    System.out.println("Jogador desconectado: " + playerId);

                    // Remove o jogador
                    players.remove(playerId);
                    if (playerId.equals(firstPlayerConnected)) {
                        firstPlayerConnected = null;
                    }

                    // Notifica os jogadores restantes
                    for (String remainingPlayer : players.keySet()) {
                        ((SocketGameServer)server).sendToPlayer(remainingPlayer,
                                NetworkProtocol.GAME_END, "Oponente desconectou");
                    }

                    // Limpa o registro de jogadores
                    players.clear();
                    firstPlayerConnected = null;
                }

                @Override
                public void onMessageReceived(String playerId, String command, String data) {
                    System.out.println("Mensagem recebida de " + playerId + ": " + command + " | " + data);

                    // Repassa a mensagem para o outro jogador
                    for (String otherPlayerId : players.keySet()) {
                        if (!otherPlayerId.equals(playerId)) {
                            ((SocketGameServer)server).sendToPlayer(otherPlayerId, command, data);
                        }
                    }

                    // Tratamento especial para surrender
                    if (command.equals(NetworkProtocol.SURRENDER)) {
                        handleSurrender(server, playerId);
                    }
                }
            });

            // Inicia o servidor na porta 12345
            System.out.println("Iniciando servidor na porta 12345...");
            server.start(12345);

            // Adiciona shutdown hook para parar o servidor graciosamente
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Parando servidor...");
                server.stop();
            }));

        } catch (CommunicationException e) {
            System.err.println("Erro ao iniciar servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void startGame(GameServerCommunication server) {
        // Envia mensagem de início para cada jogador
        for (PlayerInfo player : players.values()) {
            String startMessage = player.isFirstPlayer ? "FIRST" : "SECOND";
            ((SocketGameServer)server).sendToPlayer(player.id,
                    NetworkProtocol.GAME_START, startMessage);
        }
        System.out.println("Jogo iniciado com " + players.size() + " jogadores");
    }

    private static void handleSurrender(GameServerCommunication server, String surrenderingPlayer) {
        // Notifica todos os jogadores sobre a desistência
        for (String playerId : players.keySet()) {
            String message = playerId.equals(surrenderingPlayer) ?
                    "Você desistiu" : "Oponente desistiu";
            ((SocketGameServer)server).sendToPlayer(playerId,
                    NetworkProtocol.GAME_END, message);
        }

        // Limpa o estado do jogo
        players.clear();
        firstPlayerConnected = null;
    }
}