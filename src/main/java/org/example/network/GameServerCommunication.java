package org.example.network;

import org.example.communication.CommunicationException;

public interface GameServerCommunication {
    void start(int port) throws CommunicationException;
    void stop();
    void setServerListener(GameServerListener listener);
}
