package org.example.common;

// Enum que define os tipos de comunicação possíveis no projeto.
// Atualmente só existe SOCKET, mas já está preparado para RPC no futuro.
public enum CommunicationType {
    SOCKET,
    RPC  // Para futura implementação
}