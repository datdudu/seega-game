package org.example.communication.socket;

import org.example.communication.*;
import org.example.network.NetworkProtocol;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SocketCommunication implements GameCommunication {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private BlockingQueue<String> messageQueue;
    private volatile boolean running;
    private GameCommunicationListener listener;

    @Override
    public void connect(String host, int port) throws CommunicationException {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            messageQueue = new LinkedBlockingQueue<>();
            running = true;

            // Inicia thread de recebimento
            new Thread(this::receiveMessages).start();
        } catch (IOException e) {
            throw new CommunicationException("Erro ao conectar: " + e.getMessage(), e);
        }
    }

    @Override
    public void disconnect() {
        running = false;
        try {
            if (out != null) {
                out.println(NetworkProtocol.createMessage(NetworkProtocol.SURRENDER, ""));
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMove(int fromRow, int fromCol, int toRow, int toCol) {
        String moveData = fromRow + "," + fromCol + "," + toRow + "," + toCol;
        out.println(NetworkProtocol.createMessage(NetworkProtocol.MOVE, moveData));
    }

    @Override
    public void sendChat(String message) {
        out.println(NetworkProtocol.createMessage(NetworkProtocol.CHAT, message));
    }

    @Override
    public void surrender() {
        out.println(NetworkProtocol.createMessage(NetworkProtocol.SURRENDER, ""));
        disconnect();
    }

    @Override
    public void setGameCommunicationListener(GameCommunicationListener listener) {
        this.listener = listener;
    }

    private void receiveMessages() {
        try {
            String message;
            while (running && (message = in.readLine()) != null) {
                String[] parts = NetworkProtocol.parseMessage(message);
                String command = parts[0];
                String data = parts[1];

                if (listener != null) {
                    switch (command) {
                        case NetworkProtocol.GAME_START:
                            listener.onGameStart("FIRST".equals(data));
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
            }
        } catch (IOException e) {
            if (running && listener != null) {
                listener.onError("Conexão perdida: " + e.getMessage());
            }
        }
    }
}
