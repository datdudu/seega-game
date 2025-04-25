package org.example.network.socket;

import org.example.communication.*;
import org.example.network.GameServerCommunication;
import org.example.network.GameServerListener;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * Implementação do servidor de jogo usando sockets TCP.
 * Gerencia conexões de jogadores e comunicação em rede.
 */
public class SocketGameServer implements GameServerCommunication {
    // Socket do servidor para aceitar conexões
    private ServerSocket serverSocket;

    // Pool de threads para gerenciar múltiplos jogadores
    private ExecutorService playerPool;

    // Mapa thread-safe para armazenar os escritores de cada jogador
    private ConcurrentHashMap<String, PrintWriter> players;

    // Listener para notificar eventos do servidor
    private GameServerListener listener;

    // Flag para controlar o estado de execução do servidor
    private volatile boolean running = false;

    /**
     * Construtor que inicializa o mapa de jogadores
     */
    public SocketGameServer() {
        players = new ConcurrentHashMap<>();
    }

    /**
     * Inicia o servidor na porta especificada
     * @param port Porta onde o servidor vai escutar
     * @throws CommunicationException se houver erro ao iniciar o servidor
     */
    @Override
    public void start(int port) throws CommunicationException {
        try {
            // Cria o socket do servidor
            serverSocket = new ServerSocket(port);

            // Cria pool com 2 threads (uma para cada jogador)
            playerPool = Executors.newFixedThreadPool(2);

            // Marca servidor como em execução
            running = true;

            // Inicia loop de aceitação de conexões em thread separada
            new Thread(this::acceptLoop).start();

            System.out.println("Servidor Socket iniciado na porta " + port);
        } catch (IOException e) {
            throw new CommunicationException("Erro ao iniciar servidor: " + e.getMessage(), e);
        }
    }

    /**
     * Loop principal que aceita novas conexões de jogadores
     */
    private void acceptLoop() {
        while (running) {
            try {
                // Aceita nova conexão
                Socket playerSocket = serverSocket.accept();

                // Gera ID único para o jogador baseado no endereço
                String playerId = playerSocket.getRemoteSocketAddress().toString();

                // Cria writer para enviar mensagens para este jogador
                PrintWriter out = new PrintWriter(playerSocket.getOutputStream(), true);
                players.put(playerId, out);

                // Notifica sobre nova conexão
                if (listener != null) listener.onPlayerConnected(playerId);

                // Cria e executa handler para este jogador
                playerPool.execute(new PlayerHandler(playerSocket, playerId));
            } catch (IOException e) {
                // Só imprime erro se o servidor ainda estiver rodando
                if (running) e.printStackTrace();
            }
        }
    }

    /**
     * Para o servidor e limpa recursos
     */
    @Override
    public void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
            if (playerPool != null) playerPool.shutdownNow();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Define o listener para eventos do servidor
     */
    @Override
    public void setServerListener(GameServerListener listener) {
        this.listener = listener;
    }

    /**
     * Envia mensagem para um jogador específico
     * @param playerId ID do jogador
     * @param command Comando a ser enviado
     * @param data Dados do comando
     */
    public void sendToPlayer(String playerId, String command, String data) {
        PrintWriter out = players.get(playerId);
        if (out != null) {
            out.println(org.example.network.NetworkProtocol.createMessage(command, data));
        }
    }

    /**
     * Envia mensagem para todos os jogadores conectados
     * @param command Comando a ser enviado
     * @param data Dados do comando
     */
    public void broadcast(String command, String data) {
        String msg = org.example.network.NetworkProtocol.createMessage(command, data);
        for (PrintWriter out : players.values()) {
            out.println(msg);
        }
    }

    /**
     * Classe interna que gerencia a comunicação com um jogador específico
     */
    private class PlayerHandler implements Runnable {
        private Socket socket;          // Socket do jogador
        private String playerId;        // ID único do jogador
        private BufferedReader in;      // Reader para receber mensagens

        /**
         * Construtor que inicializa a conexão com o jogador
         */
        public PlayerHandler(Socket socket, String playerId) throws IOException {
            this.socket = socket;
            this.playerId = playerId;
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        /**
         * Loop principal de processamento de mensagens do jogador
         */
        @Override
        public void run() {
            try {
                String inputLine;
                // Lê mensagens enquanto houver conexão
                while ((inputLine = in.readLine()) != null) {
                    // Processa a mensagem recebida
                    String[] parts = org.example.network.NetworkProtocol.parseMessage(inputLine);
                    String command = parts[0];
                    String data = parts[1];

                    // Notifica o listener sobre a mensagem
                    if (listener != null) {
                        listener.onMessageReceived(playerId, command, data);
                    }
                }
            } catch (IOException e) {
                // Silenciosamente ignora erros de IO (comum em desconexões)
            } finally {
                // Limpa recursos quando o jogador desconecta
                try {
                    players.remove(playerId);
                    if (listener != null) listener.onPlayerDisconnected(playerId);
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
