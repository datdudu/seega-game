package org.example;// Client.java
import org.example.CommunicationInterface;
import org.example.SocketCommunication;

import java.io.*;

public class Client {
    private CommunicationInterface communication;
    private String name;

    public Client() {
        this.communication = new SocketCommunication();
    }

    private String askName() {
        System.out.println("Digite seu nome de usuário: ");
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
        try {
            String name = consoleReader.readLine();
            while (name == null || name.trim().isEmpty()) {
                System.out.println("Nome inválido. Por favor, digite um nome válido: ");
                name = consoleReader.readLine();
            }
            return name.trim();
        } catch (IOException e) {
            System.out.println("Erro ao ler o nome. Usando nome padrão 'Usuário'");
            return "Usuário";
        }
    }

    public void start(String host, int port) {
        try {
            // Pede o nome do usuário antes de conectar
            this.name = askName();
            System.out.println("Bem-vindo(a), " + this.name + "!");
            System.out.println("Digite suas mensagens abaixo. Para sair, pressione Ctrl+C");

            // Conecta ao servidor
            communication.connect(host, port);

            // Thread para receber mensagens
            new Thread(() -> {
                try {
                    while (true) {
                        String message = communication.receiveMessage();
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    System.out.println("Conexão com o servidor perdida.");
                }
            }).start();

            // Thread para enviar mensagens
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            String message;
            while ((message = consoleReader.readLine()) != null) {
                if (!message.trim().isEmpty()) {
                    communication.sendMessage(name + ": " + message);
                }
            }

        } catch (IOException e) {
            System.out.println("Erro de conexão: " + e.getMessage());
        } finally {
            try {
                communication.disconnect();
            } catch (IOException e) {
                System.out.println("Erro ao desconectar: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.start("localhost", 12345);
    }
}