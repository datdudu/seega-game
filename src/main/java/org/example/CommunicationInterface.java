package org.example;

import java.io.IOException;

// CommunicationInterface.java
public interface CommunicationInterface {
    void connect(String host, int port) throws IOException;
    void disconnect() throws IOException;
    void sendMessage(String message) throws IOException;
    String receiveMessage() throws IOException;
}
