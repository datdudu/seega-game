package org.example.network;

/**
 * Classe que define o protocolo de comunicação entre cliente e servidor.
 * Estabelece um padrão de mensagens e fornece métodos para manipulá-las.
 */
public class NetworkProtocol {
    // Constantes que definem todos os tipos de comandos possíveis no protocolo

    /** Comando para estabelecer conexão inicial */
    public static final String CONNECT = "CONNECT";

    /** Comando para movimentação de peças no tabuleiro */
    public static final String MOVE = "MOVE";

    /** Comando para mensagens de chat entre jogadores */
    public static final String CHAT = "CHAT";

    /** Comando para quando um jogador desiste da partida */
    public static final String SURRENDER = "SURRENDER";

    /** Comando para sinalizar início do jogo */
    public static final String GAME_START = "GAME_START";

    /** Comando para sinalizar fim do jogo */
    public static final String GAME_END = "GAME_END";

    /**
     * Cria uma mensagem formatada para o protocolo.
     * O formato padrão é: "COMANDO|DADOS"
     * Exemplo: "MOVE|2,3,4,5" ou "CHAT|Olá, como vai?"
     *
     * @param command O comando a ser enviado (deve ser uma das constantes definidas)
     * @param data Os dados associados ao comando
     * @return Uma string formatada seguindo o protocolo
     */
    public static String createMessage(String command, String data) {
        // Garante que data nunca seja null para evitar erros de formatação
        data = (data == null) ? "" : data;
        // Retorna a mensagem no formato COMANDO|DADOS
        return command + "|" + data;
    }

    /**
     * Interpreta uma mensagem recebida, separando-a em comando e dados.
     *
     * @param message A mensagem completa recebida
     * @return Um array onde:
     *         - índice 0: contém o comando
     *         - índice 1: contém os dados (ou string vazia se não houver dados)
     */
    public static String[] parseMessage(String message) {
        // Divide a mensagem usando | como separador, limitando a 2 partes
        String[] parts = message.split("\\|", 2);

        // Se a mensagem não contiver dados, retorna um array com dados vazios
        if (parts.length == 1) {
            return new String[]{parts[0], ""};
        }
        return parts;
    }
}
