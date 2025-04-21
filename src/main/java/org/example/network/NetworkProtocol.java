package org.example.network;


public class NetworkProtocol {
    // Comandos do protocolo
    public static final String CONNECT = "CONNECT";
    public static final String MOVE = "MOVE";
    public static final String CHAT = "CHAT";
    public static final String SURRENDER = "SURRENDER";
    public static final String GAME_START = "GAME_START";
    public static final String GAME_END = "GAME_END";

    // Formato da mensagem: COMANDO|DADOS
    public static String createMessage(String command, String data) {
        // Garante que data nunca seja null
        data = (data == null) ? "" : data;
        return command + "|" + data;
    }

    public static String[] parseMessage(String message) {
        String[] parts = message.split("\\|", 2); // Limite 2 para garantir apenas uma divisão
        // Garante que sempre retorne um array com 2 elementos
        if (parts.length == 1) {
            return new String[]{parts[0], ""};
        }
        return parts;
    }
}
