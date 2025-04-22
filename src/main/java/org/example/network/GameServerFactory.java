package org.example.network;


import org.example.common.CommunicationType;
import org.example.network.socket.SocketGameServer;

public class GameServerFactory {
    public static GameServerCommunication createServer(CommunicationType type) {
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
