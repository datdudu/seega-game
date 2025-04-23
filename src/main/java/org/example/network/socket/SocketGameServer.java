package org.example.network.socket;

import org.example.communication.*;
import org.example.network.GameServerCommunication;
import org.example.network.GameServerListener;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class SocketGameServer implements GameServerCommunication {
    private ServerSocket serverSocket;
    private ExecutorService playerPool;
    private ConcurrentHashMap<String, PrintWriter> players;
    private GameServerListener listener;
    private volatile boolean running = false;

    public SocketGameServer() {
        players = new ConcurrentHashMap<>();
    }

    @Override
    public void start(int port) throws CommunicationException {
        try {
            serverSocket = new ServerSocket(port);
            playerPool = Executors.newFixedThreadPool(2);
            running = true;
            new Thread(this::acceptLoop).start();
            System.out.println("Servidor Socket iniciado na porta " + port);
        } catch (IOException e) {
            throw new CommunicationException("Erro ao iniciar servidor: " + e.getMessage(), e);
        }
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket playerSocket = serverSocket.accept();
                String playerId = playerSocket.getRemoteSocketAddress().toString();
                PrintWriter out = new PrintWriter(playerSocket.getOutputStream(), true);
                players.put(playerId, out);

                if (listener != null) listener.onPlayerConnected(playerId);

                playerPool.execute(new PlayerHandler(playerSocket, playerId));
            } catch (IOException e) {
                if (running) e.printStackTrace();
            }
        }
    }

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

    @Override
    public void setServerListener(GameServerListener listener) {
        this.listener = listener;
    }

    // Envia mensagem para um jogador específico
    public void sendToPlayer(String playerId, String command, String data) {
        PrintWriter out = players.get(playerId);
        if (out != null) {
            out.println(org.example.network.NetworkProtocol.createMessage(command, data));
        }
    }

    // Broadcast para todos os jogadores
    public void broadcast(String command, String data) {
        String msg = org.example.network.NetworkProtocol.createMessage(command, data);
        for (PrintWriter out : players.values()) {
            out.println(msg);
        }
    }

    private class PlayerHandler implements Runnable {
        private Socket socket;
        private String playerId;
        private BufferedReader in;

        public PlayerHandler(Socket socket, String playerId) throws IOException {
            this.socket = socket;
            this.playerId = playerId;
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        @Override
        public void run() {
            try {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    String[] parts = org.example.network.NetworkProtocol.parseMessage(inputLine);
                    String command = parts[0];
                    String data = parts[1];
                    if (listener != null) {
                        listener.onMessageReceived(playerId, command, data);
                    }
                }
            } catch (IOException e) {
                // e.printStackTrace();
            } finally {
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
