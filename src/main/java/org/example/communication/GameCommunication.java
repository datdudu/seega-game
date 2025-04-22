package org.example.communication;



public interface GameCommunication {
    void connect(String host, int port) throws CommunicationException;
    void disconnect();
    void sendMove(int fromRow, int fromCol, int toRow, int toCol);
    void sendChat(String message);
    void surrender();
    void setGameCommunicationListener(GameCommunicationListener listener);
}
