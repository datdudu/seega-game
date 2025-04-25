package org.example.notUsableAnymore;

import org.example.network.NetworkProtocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameServer {
    private static final int MAX_PLAYERS = 2;
    private ServerSocket serverSocket;
    private ExecutorService playerPool;
    private ConcurrentHashMap<Socket, PrintWriter> players;
    private Socket firstPlayerSocket; // Armazena referência ao primeiro jogador

    public GameServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        playerPool = Executors.newFixedThreadPool(MAX_PLAYERS);
        players = new ConcurrentHashMap<>();
        System.out.println("Servidor iniciado na porta " + port);
    }

    public void start() {
        while (true) {
            try {
                Socket playerSocket = serverSocket.accept();
                if (players.size() >= MAX_PLAYERS) {
                    PrintWriter out = new PrintWriter(playerSocket.getOutputStream(), true);
                    out.println(NetworkProtocol.createMessage("ERROR", "Servidor cheio"));
                    playerSocket.close();
                    continue;
                }

                PrintWriter out = new PrintWriter(playerSocket.getOutputStream(), true);
                players.put(playerSocket, out);

                // Define o primeiro jogador
                if (firstPlayerSocket == null) {
                    firstPlayerSocket = playerSocket;
                }

                PlayerHandler handler = new PlayerHandler(playerSocket);
                playerPool.execute(handler);

                // Quando dois jogadores se conectarem, inicia o jogo
                if (players.size() == MAX_PLAYERS) {
                    // Envia mensagem específica para cada jogador
                    players.get(firstPlayerSocket).println(
                            NetworkProtocol.createMessage(NetworkProtocol.GAME_START, "FIRST"));

                    // Envia para o segundo jogador
                    for (Socket socket : players.keySet()) {
                        if (socket != firstPlayerSocket) {
                            players.get(socket).println(
                                    NetworkProtocol.createMessage(NetworkProtocol.GAME_START, "SECOND"));
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void broadcastMessage(String command, String data) {
        String message = NetworkProtocol.createMessage(command, data);
        for (PrintWriter out : players.values()) {
            out.println(message);
        }
    }

    private class PlayerHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;

        public PlayerHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        @Override
        public void run() {
            try {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    String[] parts = NetworkProtocol.parseMessage(inputLine);
                    String command = parts[0];
                    String data = parts[1];

                    // Repassar mensagem para outro jogador
                    for (Socket playerSocket : players.keySet()) {
                        if (playerSocket != socket) {
                            players.get(playerSocket).println(inputLine);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    players.remove(socket);
                    // Se o primeiro jogador desconectou, atualiza a referência
                    if (socket == firstPlayerSocket) {
                        firstPlayerSocket = null;
                    }
                    socket.close();

                    // Notifica os jogadores restantes sobre a desconexão
                    broadcastMessage(NetworkProtocol.GAME_END, "Oponente desconectou");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            GameServer server = new GameServer(12345);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}