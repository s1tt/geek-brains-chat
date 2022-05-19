package com.geekbrains.chat.server;

import com.geekbrains.chat.Command;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ChatServer {

    private final Map<String, ClientHandler> clients;
    private static final Logger LOGGER = LogManager.getLogger(ChatServer.class);

    public ChatServer() {
        this.clients = new HashMap<>();
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(8189);
             AuthService authService = new DbAuthService()) {
            LOGGER.info("Server has been started");
            while (true) {
                LOGGER.info("Wait client connection...");
                final Socket socket = serverSocket.accept();
                new ClientHandler(socket, this, authService);
                LOGGER.info("Client connected");
            }
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }

    public void sendMsgToClient(ClientHandler sender, String to, String message) {
        ClientHandler receiver = clients.get(to);
        if (receiver != null) {
            receiver.sendMessage("from " + sender.getNick() + ": " + message);
            sender.sendMessage("to " + to + ": " + message);
        } else {
            sender.sendMessage(Command.ERROR, "User with nickname " + to + " is not in the chat");
        }
    }

    public boolean isNickBusy(String nick) {
        return clients.containsKey(nick);
    }

    public void subscribe(ClientHandler client) {
        clients.put(client.getNick(), client);
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client.getNick());
        broadcastClientList();
    }

    private void broadcastClientList() {
        String nicks = clients.values().stream()
                .map(ClientHandler::getNick)
                .collect(Collectors.joining(" "));
        broadcast(Command.CLIENTS, nicks.trim());
    }

    private void broadcast(Command command, String nicks) {
        for (ClientHandler client : clients.values()) {
            client.sendMessage(command, nicks);
        }
    }

    public void broadcast(String msg) {
        LOGGER.info(msg);
        clients.values().forEach(client -> client.sendMessage(msg));
    }
}



