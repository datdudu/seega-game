package org.example;// Server.java
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

// Server.java
public class Server {
    private ServerSocket serverSocket;
    private List<ClientHandler> clients;
    private SimpleDateFormat timeFormat;
    private GameState gameState;

    private static class GameState {
        ClientHandler player1;
        ClientHandler player2;
        boolean isGameStarted;
        boolean isPlayer1Turn;

        public GameState() {
            this.isGameStarted = false;
            this.isPlayer1Turn = true;
        }

        public boolean isFull() {
            return player1 != null && player2 != null;
        }

        public void reset() {
            player1 = null;
            player2 = null;
            isGameStarted = false;
            isPlayer1Turn = true;
        }
    }

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        clients = new ArrayList<>();
        timeFormat = new SimpleDateFormat("HH:mm:ss");
        gameState = new GameState();

        System.out.println("=================================");
        System.out.println("Servidor iniciado na porta " + port);
        System.out.println("Aguardando jogadores...");
        System.out.println("=================================");
    }

    public void start() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);
                handleNewConnection(clientHandler);
                new Thread(clientHandler).start();

                System.out.println("\nNovo cliente conectado: " + clientSocket.getInetAddress());
                System.out.println("Total de clientes conectados: " + clients.size());
            } catch (IOException e) {
                System.out.println("Erro na conexão com cliente: " + e.getMessage());
            }
        }
    }

    private void handleNewConnection(ClientHandler client) {
        if (gameState.player1 == null) {
            gameState.player1 = client;
            client.sendMessage("ROLE:PLAYER1");
        } else if (gameState.player2 == null) {
            gameState.player2 = client;
            client.sendMessage("ROLE:PLAYER2");

            // Iniciar o jogo quando o segundo jogador se conectar
            gameState.isGameStarted = true;
            broadcastGameStart();
        } else {
            client.sendMessage("ERROR:Jogo já está em andamento");
        }
    }

    private void broadcastGameStart() {
        String player1Name = gameState.player1.getPlayerName();
        String player2Name = gameState.player2.getPlayerName();

        gameState.player1.sendMessage("GAME_START:" + player1Name + "," + player2Name);
        gameState.player2.sendMessage("GAME_START:" + player1Name + "," + player2Name);
    }

    public void processCommand(String message, ClientHandler sender) {
        if (message.startsWith("NAME:")) {
            handleNameCommand(message, sender);
        } else if (message.startsWith("MOVE:")) {
            handleMoveCommand(message, sender);
        } else if (message.startsWith("PASS_TURN")) {
            handlePassTurn(sender);
        } else if (message.startsWith("FORFEIT")) {
            handleForfeit(sender);
        } else {
            // Mensagens normais do chat
            broadcast(message, sender);
        }
    }

    private void handleNameCommand(String message, ClientHandler sender) {
        String name = message.substring(5);
        sender.setPlayerName(name);

        if (gameState.isFull()) {
            broadcastGameStart();
        }
    }

    private void handleMoveCommand(String message, ClientHandler sender) {
        if (!isValidTurn(sender)) {
            sender.sendMessage("ERROR:Não é sua vez");
            return;
        }

        // Repassar o movimento para o outro jogador
        ClientHandler otherPlayer = (sender == gameState.player1) ? gameState.player2 : gameState.player1;
        otherPlayer.sendMessage(message);

        // Trocar o turno
        gameState.isPlayer1Turn = !gameState.isPlayer1Turn;
    }

    private void handlePassTurn(ClientHandler sender) {
        if (!isValidTurn(sender)) {
            sender.sendMessage("ERROR:Não é sua vez");
            return;
        }

        // Trocar o turno e notificar os jogadores
        gameState.isPlayer1Turn = !gameState.isPlayer1Turn;
        gameState.player1.sendMessage("TURN:" + (gameState.isPlayer1Turn ? "YOUR_TURN" : "OPPONENT_TURN"));
        gameState.player2.sendMessage("TURN:" + (!gameState.isPlayer1Turn ? "YOUR_TURN" : "OPPONENT_TURN"));
    }

    private void handleForfeit(ClientHandler sender) {
        String winner = (sender == gameState.player1) ? gameState.player2.getPlayerName() : gameState.player1.getPlayerName();
        broadcast("GAME_END:FORFEIT:" + winner, null);
        gameState.reset();
    }

    private boolean isValidTurn(ClientHandler sender) {
        return (gameState.isPlayer1Turn && sender == gameState.player1) ||
                (!gameState.isPlayer1Turn && sender == gameState.player2);
    }

    public void broadcast(String message, ClientHandler sender) {
        String time = timeFormat.format(new Date());
        String formattedMessage = "[" + time + "] " + message;

        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(formattedMessage);
            }
        }
    }

    public void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);

        if (clientHandler == gameState.player1 || clientHandler == gameState.player2) {
            // Se um jogador desconectar, notificar o outro e resetar o jogo
            broadcast("GAME_END:DISCONNECT", clientHandler);
            gameState.reset();
        }

        System.out.println("\nCliente desconectado");
        System.out.println("Total de clientes conectados: " + clients.size());
    }

    public static void main(String[] args) {
        try {
            Server server = new Server(12345);
            server.start();
        } catch (IOException e) {
            System.out.println("Erro ao iniciar servidor: " + e.getMessage());
        }
    }
}