package org.example.network.rpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.example.communication.CommunicationException;
import org.example.communication.rpc.SeegaGameGrpc;
import org.example.communication.rpc.SeegaProto;
import org.example.network.GameServerCommunication;
import org.example.network.GameServerListener;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RPCGameServer implements GameServerCommunication {
    private Server server;
    private final ConcurrentHashMap<String, StreamObserver<SeegaProto.GameMessage>> clients = new ConcurrentHashMap<>();
    private final AtomicInteger clientCounter = new AtomicInteger(0);
    private GameServerListener listener;
    private String firstPlayerId = null;

    @Override
    public void start(int port) throws CommunicationException {
        try {
            server = ServerBuilder.forPort(port)
                    .addService(new SeegaGameImpl())
                    .build()
                    .start();
        } catch (IOException e) {
            throw new CommunicationException("Erro ao iniciar servidor RPC: " + e.getMessage(), e);
        }
    }

    @Override
    public void stop() {
        if (server != null) server.shutdown();
    }

    @Override
    public void setServerListener(GameServerListener listener) {
        this.listener = listener;
    }

    @Override
    public void sendToPlayer(String playerId, String command, String data) {
        SeegaProto.GameMessage msg = SeegaProto.GameMessage.newBuilder()
                .setCommand(command)
                .setData(data)
                .setPlayerId(playerId)
                .build();
        StreamObserver<SeegaProto.GameMessage> obs = clients.get(playerId);
        if (obs != null) obs.onNext(msg);
    }

    private class SeegaGameImpl extends SeegaGameGrpc.SeegaGameImplBase {
        @Override
        public StreamObserver<SeegaProto.GameMessage> gameStream(StreamObserver<SeegaProto.GameMessage> responseObserver) {
            // Verifica se já há dois clientes conectados
            if (clients.size() >= 2) {
                // Envia mensagem de erro apenas para este cliente
                SeegaProto.GameMessage errorMsg = SeegaProto.GameMessage.newBuilder()
                        .setCommand("ERROR")
                        .setData("Servidor cheio")
                        .build();
                responseObserver.onNext(errorMsg);
                responseObserver.onCompleted();
                return new StreamObserver<SeegaProto.GameMessage>() {
                    @Override public void onNext(SeegaProto.GameMessage value) {}
                    @Override public void onError(Throwable t) {}
                    @Override public void onCompleted() {}
                };
            }

            // Se não está cheio, prossegue normalmente
            String clientId = String.valueOf(clientCounter.incrementAndGet());
            clients.put(clientId, responseObserver);

            if (listener != null) listener.onPlayerConnected(clientId);

            // Inicia o jogo se ambos conectaram
            if (clients.size() == 2) {
                int i = 0;
                for (String id : clients.keySet()) {
                    String startMsg = (i == 0) ? "FIRST" : "SECOND";
                    sendToPlayer(id, org.example.network.NetworkProtocol.GAME_START, startMsg);
                    i++;
                }
            }

            return new StreamObserver<SeegaProto.GameMessage>() {
                @Override
                public void onNext(SeegaProto.GameMessage msg) {
                    if (listener != null) {
                        listener.onMessageReceived(clientId, msg.getCommand(), msg.getData());
                    }
                }

                @Override
                public void onError(Throwable t) {
                    clients.remove(clientId);
                    if (listener != null) listener.onPlayerDisconnected(clientId);
                }

                @Override
                public void onCompleted() {
                    clients.remove(clientId);
                    if (listener != null) listener.onPlayerDisconnected(clientId);
                    responseObserver.onCompleted();
                }
            };
        }
    }
}