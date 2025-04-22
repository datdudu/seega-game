package org.example.communication;

public interface GameCommunicationListener {
    void onGameStart(boolean isFirstPlayer);
    void onMoveReceived(String moveData);
    void onChatReceived(String message);
    void onGameEnd(String reason);
    void onError(String error);
}
