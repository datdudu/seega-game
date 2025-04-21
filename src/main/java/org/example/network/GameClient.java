package org.example.network;

import java.net.*;
import java.io.*;
import java.util.concurrent.*;

public class GameClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private BlockingQueue<String> messageQueue;
    private volatile boolean running;
    private GameClientListener listener;

    public interface GameClientListener {
        void onGameStart(boolean isFirstPlayer);
        void onMoveReceived(String moveData);
        void onChatReceived(String message);
        void onGameEnd(String reason);
        void onError(String error);
    }

    public GameClient(String host, int port, GameClientListener listener) throws IOException {
        this.socket = new Socket(host, port);
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.messageQueue = new LinkedBlockingQueue<>();
        this.listener = listener;
        this.running = true;

        // Inicia thread de recebimento
        new Thread(this::receiveMessages).start();
    }

    private void receiveMessages() {
        try {
            String message;
            while (running && (message = in.readLine()) != null) {
                String[] parts = NetworkProtocol.parseMessage(message);
                String command = parts[0];
                String data = parts[1]; // Agora garantido que sempre terá um valor

                switch (command) {
                    case NetworkProtocol.GAME_START:
                        boolean isFirstPlayer = "FIRST".equals(data);
                        listener.onGameStart(isFirstPlayer);
                        break;
                    case NetworkProtocol.MOVE:
                        listener.onMoveReceived(data);
                        break;
                    case NetworkProtocol.CHAT:
                        listener.onChatReceived(data);
                        break;
                    case NetworkProtocol.GAME_END:
                        listener.onGameEnd(data);
                        break;
                    case "ERROR":
                        listener.onError(data);
                        break;
                }
            }
        } catch (IOException e) {
            if (running) {
                listener.onError("Conexão perdida: " + e.getMessage());
            }
        }
    }

    public void sendMove(int fromRow, int fromCol, int toRow, int toCol) {
        String moveData = fromRow + "," + fromCol + "," + toRow + "," + toCol;
        out.println(NetworkProtocol.createMessage(NetworkProtocol.MOVE, moveData));
    }

    public void sendChat(String message) {
        out.println(NetworkProtocol.createMessage(NetworkProtocol.CHAT, message));
    }

    public void surrender() {
        out.println(NetworkProtocol.createMessage(NetworkProtocol.SURRENDER, ""));
        close();
    }

    public void close() {
        running = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
