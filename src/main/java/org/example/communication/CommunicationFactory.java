package org.example.communication;

import org.example.common.CommunicationType;
import org.example.communication.socket.SocketCommunication;

/**
 * Fábrica para criar instâncias de comunicação de acordo com o tipo selecionado.
 * Permite trocar facilmente entre diferentes tecnologias de comunicação (ex: Socket, RPC).
 */
public class CommunicationFactory {

    /**
     * Cria uma instância de comunicação baseada no tipo informado.
     * @param type Tipo de comunicação desejado (SOCKET, RPC, etc)
     * @return Instância de GameCommunication correspondente
     */
    public static GameCommunication createCommunication(CommunicationType type) {
        switch (type) {
            case SOCKET:
                // Retorna implementação baseada em sockets
                return new SocketCommunication();
            case RPC:
                // Futuro: implementação para RPC
                // return new RPCCommunication();
                throw new UnsupportedOperationException("RPC ainda não implementado");
            default:
                throw new IllegalArgumentException("Tipo de comunicação não suportado");
        }
    }
}
