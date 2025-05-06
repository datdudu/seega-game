package org.example.network;

import org.example.common.CommunicationType;
import org.example.communication.*;
import org.example.network.NetworkProtocol;
import org.example.network.socket.SocketGameServer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Servidor principal do jogo Seega.
 * Responsável por gerenciar conexões dos jogadores e coordenar a comunicação entre eles.
 */
public class MainServer {
    // Número máximo de jogadores permitidos (2 para o jogo Seega)
    private static final int MAX_PLAYERS = 2;

    // Mapa thread-safe para armazenar os jogadores conectados
    private static Map<String, PlayerInfo> players = new ConcurrentHashMap<>();

    // Armazena o ID do primeiro jogador que se conectou
    private static String firstPlayerConnected = null;

    /**
     * Classe interna para armazenar informações de cada jogador conectado
     */
    private static class PlayerInfo {
        String id;                // ID único do jogador
        boolean isFirstPlayer;    // Indica se é o primeiro jogador (começa o jogo)

        PlayerInfo(String id, boolean isFirstPlayer) {
            this.id = id;
            this.isFirstPlayer = isFirstPlayer;
        }
    }

    /**
     * Método principal que inicia o servidor
     */
    public static void main(String[] args) {
        // Configurações padrão do servidor
        int port = 12345;
        CommunicationType serverType = CommunicationType.SOCKET;

        // Processa argumentos da linha de comando para porta e tipo de servidor
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
                if (args.length > 1) {
                    serverType = CommunicationType.valueOf(args[1]);
                }
            } catch (NumberFormatException e) {
                System.err.println("Porta inválida! Usando porta padrão 12345");
            } catch (IllegalArgumentException e) {
                System.err.println("Tipo de servidor inválido! Usando Socket");
                serverType = CommunicationType.SOCKET;
            }
        }

        try {
            // Cria uma instância do servidor usando o padrão Factory
            GameServerCommunication server = GameServerFactory.createServer(serverType);

            // Configura o listener para eventos do servidor usando classe anônima
            server.setServerListener(new GameServerListener() {
                /**
                 * Chamado quando um novo jogador se conecta
                 */
                @Override
                public void onPlayerConnected(String playerId) {
                    System.out.println("Jogador conectado: " + playerId);

                    // Verifica se o servidor já está cheio
                    if (players.size() >= MAX_PLAYERS) {
                        server.sendToPlayer(playerId, "ERROR", "Servidor cheio");
                        return;
                    }

                    // Determina se é o primeiro jogador
                    boolean isFirstPlayer = (firstPlayerConnected == null);
                    if (isFirstPlayer) {
                        firstPlayerConnected = playerId;
                    }

                    // Registra o novo jogador
                    players.put(playerId, new PlayerInfo(playerId, isFirstPlayer));

                    // Inicia o jogo se atingiu o número necessário de jogadores
                    if (players.size() == MAX_PLAYERS) {
                        startGame(server);
                    }
                }

                /**
                 * Chamado quando um jogador se desconecta
                 */
                @Override
                public void onPlayerDisconnected(String playerId) {
                    System.out.println("Jogador desconectado: " + playerId);

                    // Remove o jogador desconectado
                    players.remove(playerId);
                    if (playerId.equals(firstPlayerConnected)) {
                        firstPlayerConnected = null;
                    }

                    // Notifica os jogadores restantes sobre a desconexão
                    for (String remainingPlayer : players.keySet()) {
                        server.sendToPlayer(remainingPlayer,
                                NetworkProtocol.GAME_END, "Oponente desconectou");
                    }

                    // Reseta o estado do servidor
                    players.clear();
                    firstPlayerConnected = null;
                }

                /**
                 * Chamado quando uma mensagem é recebida de um jogador
                 */
                @Override
                public void onMessageReceived(String playerId, String command, String data) {
                    System.out.println("Mensagem recebida de " + playerId + ": " + command + " | " + data);

                    // Trata diferentes tipos de mensagens
                    if (command.equals(NetworkProtocol.GAME_END)) {
                        handleGameEnd(server, playerId, data);
                    } else if (command.equals(NetworkProtocol.SURRENDER)) {
                        handleSurrender(server, playerId);
                    } else {
                        // Repassa mensagens normais para o outro jogador
                        for (String otherPlayerId : players.keySet()) {
                            if (!otherPlayerId.equals(playerId)) {
                                server.sendToPlayer(otherPlayerId, command, data);
                            }
                        }
                    }
                }
            });

            // Inicia o servidor
            System.out.println("Iniciando servidor na porta " + port + "...");
            server.start(port);

            // Configura shutdown hook para parada graciosa do servidor
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Parando servidor...");
                server.stop();
            }));

        } catch (CommunicationException e) {
            System.err.println("Erro ao iniciar servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Inicia o jogo enviando mensagens apropriadas para cada jogador
     */
    private static void startGame(GameServerCommunication server) {
        for (PlayerInfo player : players.values()) {
            String startMessage = player.isFirstPlayer ? "FIRST" : "SECOND";
            server.sendToPlayer(player.id,
                    NetworkProtocol.GAME_START, startMessage);
        }
        System.out.println("Jogo iniciado com " + players.size() + " jogadores");
    }

    /**
     * Trata a desistência de um jogador
     */
    private static void handleSurrender(GameServerCommunication server, String surrenderingPlayer) {
        for (String playerId : players.keySet()) {
            String message;
            if (playerId.equals(surrenderingPlayer)) {
                message = "Você desistiu, o seu oponente é o vencedor!";
            } else {
                message = "Seu oponente desistiu! Você é o vencedor!";
            }

            server.sendToPlayer(playerId,
                    NetworkProtocol.GAME_END, message);
        }

        // Limpa o estado do jogo
        players.clear();
        firstPlayerConnected = null;
    }

    /**
     * Trata o fim do jogo quando um jogador vence
     */
    private static void handleGameEnd(GameServerCommunication server, String winningPlayer, String reason) {
        for (String playerId : players.keySet()) {
            // Pula o vencedor pois ele já viu a mensagem localmente
            if (playerId.equals(winningPlayer)) {
                continue;
            }

            // Envia mensagem apropriada para o perdedor
            String message;
            if (reason.equals("VICTORY_CAPTURED_ALL")) {
                message = "Você perdeu! Todas as suas peças foram capturadas!";
            } else {
                message = "Você perdeu! Não há movimentos válidos disponíveis!";
            }

            server.sendToPlayer(playerId,
                    NetworkProtocol.GAME_END, message);
        }

        // Limpa o estado do jogo
        players.clear();
        firstPlayerConnected = null;
    }
}

