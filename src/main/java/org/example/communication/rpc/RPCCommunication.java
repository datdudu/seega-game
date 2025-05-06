package org.example.communication.rpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.example.communication.*;
import org.example.network.NetworkProtocol;

import java.util.concurrent.CountDownLatch;

public class RPCCommunication implements GameCommunication {
    private ManagedChannel channel;
    private SeegaGameGrpc.SeegaGameStub asyncStub;
    private StreamObserver<SeegaProto.GameMessage> requestObserver;
    private GameCommunicationListener listener;
    private String playerId;

    @Override
    public void connect(String host, int port) throws CommunicationException {
        try {
            channel = ManagedChannelBuilder.forAddress(host, port)
                    .usePlaintext()
                    .build();
            asyncStub = SeegaGameGrpc.newStub(channel);

            CountDownLatch readyLatch = new CountDownLatch(1);

            requestObserver = asyncStub.gameStream(new StreamObserver<SeegaProto.GameMessage>() {
                @Override
                public void onNext(SeegaProto.GameMessage msg) {
                    handleIncomingMessage(msg);
                }

                @Override
                public void onError(Throwable t) {
                    if (listener != null) listener.onError("Erro de comunicação RPC: " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    if (listener != null) listener.onError("Conexão RPC encerrada pelo servidor.");
                }
            });

            // Envia uma mensagem de conexão para obter um playerId (opcional)
            // Aqui, você pode gerar um UUID ou deixar vazio, pois o servidor pode atribuir
            this.playerId = ""; // Pode ser preenchido depois se necessário

        } catch (Exception e) {
            throw new CommunicationException("Erro ao conectar via RPC: " + e.getMessage(), e);
        }
    }

    @Override
    public void disconnect() {
        if (requestObserver != null) {
            requestObserver.onCompleted();
        }
        if (channel != null) {
            channel.shutdownNow();
        }
    }

    @Override
    public void sendMove(int fromRow, int fromCol, int toRow, int toCol) {
        String moveData = fromRow + "," + fromCol + "," + toRow + "," + toCol;
        sendMessage(NetworkProtocol.MOVE, moveData);
    }

    @Override
    public void sendChat(String message) {
        sendMessage(NetworkProtocol.CHAT, message);
    }

    @Override
    public void surrender() {
        sendMessage(NetworkProtocol.SURRENDER, "");
        disconnect();
    }

    @Override
    public void sendEndGame(String reason) {
        sendMessage(NetworkProtocol.GAME_END, reason);
    }

    @Override
    public void setGameCommunicationListener(GameCommunicationListener listener) {
        this.listener = listener;
    }

    private void sendMessage(String command, String data) {
        SeegaProto.GameMessage msg = SeegaProto.GameMessage.newBuilder()
                .setCommand(command)
                .setData(data)
                .setPlayerId(playerId == null ? "" : playerId)
                .build();
        requestObserver.onNext(msg);
    }

    private void handleIncomingMessage(SeegaProto.GameMessage msg) {
        if (listener == null) return;
        String command = msg.getCommand();
        String data = msg.getData();

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