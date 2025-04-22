package org.example.network;


import org.example.network.socket.SocketGameServer;

public class GameServerFactory {
    public enum ServerType { SOCKET, RPC }

    public static GameServerCommunication createServer(ServerType type) {
        switch (type) {
            case SOCKET:
                return new SocketGameServer();
            case RPC:
                throw new UnsupportedOperationException("RPC ainda não implementado");
            default:
                throw new IllegalArgumentException("Tipo de servidor não suportado");
        }
    }
}
