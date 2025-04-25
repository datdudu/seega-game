package org.example.communication;

/**
 * Interface para receber eventos de comunicação do servidor ou do outro jogador.
 * Implementada pelo controlador do jogo para reagir a eventos de rede.
 */
public interface GameCommunicationListener {
    /**
     * Notificado quando o jogo começa.
     * @param isFirstPlayer true se o jogador for o primeiro a jogar
     */
    void onGameStart(boolean isFirstPlayer);

    /**
     * Notificado quando um movimento do oponente é recebido.
     * @param moveData Dados do movimento (ex: "2,3,2,4")
     */
    void onMoveReceived(String moveData);

    /**
     * Notificado quando uma mensagem de chat é recebida.
     * @param message Mensagem recebida
     */
    void onChatReceived(String message);

    /**
     * Notificado quando o jogo termina.
     * @param reason Motivo do fim do jogo (ex: vitória, desistência)
     */
    void onGameEnd(String reason);

    /**
     * Notificado quando ocorre um erro de comunicação.
     * @param error Mensagem de erro
     */
    void onError(String error);
}
