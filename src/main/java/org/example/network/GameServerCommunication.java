package org.example.network;

import org.example.communication.CommunicationException;

// Interface para abstrair diferentes implementações de servidor de jogo.
// Permite trocar facilmente entre Socket, RPC, etc.
public interface GameServerCommunication {
    void start(int port) throws CommunicationException;
    void stop();
    void setServerListener(GameServerListener listener);

    void sendToPlayer(String playerId, String command, String data);
}
