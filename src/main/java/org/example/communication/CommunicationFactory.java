package org.example.communication;

import org.example.common.CommunicationType;
import org.example.communication.socket.SocketCommunication;

public class CommunicationFactory {


    public static GameCommunication createCommunication(CommunicationType type) {
        switch (type) {
            case SOCKET:
                return new SocketCommunication();
            case RPC:
                // return new RPCCommunication(); // Futura implementação
                throw new UnsupportedOperationException("RPC ainda não implementado");
            default:
                throw new IllegalArgumentException("Tipo de comunicação não suportado");
        }
    }
}
