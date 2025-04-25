package org.example.communication;

/**
 * Interface que define o contrato para qualquer tecnologia de comunicação cliente-servidor.
 * Permite trocar facilmente a implementação (Socket, RPC, etc) sem alterar o restante do código.
 */
public interface GameCommunication {
    /**
     * Conecta ao servidor.
     * @param host Endereço IP ou hostname do servidor
     * @param port Porta do servidor
     * @throws CommunicationException Se ocorrer erro de conexão
     */
    void connect(String host, int port) throws CommunicationException;

    /**
     * Desconecta do servidor e libera recursos.
     */
    void disconnect();

    /**
     * Envia um movimento para o servidor.
     * @param fromRow Linha de origem
     * @param fromCol Coluna de origem
     * @param toRow Linha de destino
     * @param toCol Coluna de destino
     */
    void sendMove(int fromRow, int fromCol, int toRow, int toCol);

    /**
     * Envia uma mensagem de chat para o servidor.
     * @param message Mensagem a ser enviada
     */
    void sendChat(String message);

    /**
     * Envia uma mensagem de desistência para o servidor.
     */
    void surrender();

    /**
     * Envia uma mensagem de fim de jogo para o servidor.
     * @param reason Motivo do fim do jogo
     */
    void sendEndGame(String reason);

    /**
     * Registra o listener para receber eventos de comunicação.
     * @param listener Implementação de GameCommunicationListener
     */
    void setGameCommunicationListener(GameCommunicationListener listener);
}
