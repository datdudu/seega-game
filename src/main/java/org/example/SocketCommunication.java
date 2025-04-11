package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// SocketCommunication.java
public class SocketCommunication implements CommunicationInterface {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    @Override
    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void disconnect() throws IOException {
        if (socket != null && !socket.isClosed()) {
            reader.close();
            writer.close();
            socket.close();
        }
    }

    @Override
    public void sendMessage(String message) throws IOException {
        writer.println(message);
    }

    @Override
    public String receiveMessage() throws IOException {
        return reader.readLine();
    }
}
