package org.example.network;

public interface GameServerListener {
    void onPlayerConnected(String playerId);
    void onPlayerDisconnected(String playerId);
    void onMessageReceived(String playerId, String command, String data);
}