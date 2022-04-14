package com.geekbrains.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private final Socket socket;
    private final ChatServer server;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final AuthService authService;

    private String nick;

    public ClientHandler(Socket socket, ChatServer server, AuthService authService) {
        try {
            this.nick = "";
            this.socket = socket;
            this.server = server;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.authService = authService;

            new Thread(() -> {
                try {
                    authenticate();
                    readMessages();
                } finally {
                    closeConnection();
                }
            }).start();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void closeConnection() {
        sendMessage("/end");
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (socket != null) {
                server.unsubscribe(this);
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void authenticate() {
        while (true) {
            try {
                final String str = in.readUTF();
                if (str.startsWith("/auth")) {
                    final String[] split = str.split(" ");
                    final String login = split[1];
                    final String password = split[2];
                    final String nick = authService.getNickByLoginAndPassword(login, password);
                    if (nick != null) {
                        if (server.isNickBusy(nick)) {
                            sendMessage("Пользователь уже авторизован");
                            continue;
                        }
                        sendMessage("/authok " + nick);
                        this.nick = nick;
                        server.broadcast("Пользователь " + nick + " зашел в чат");
                        server.subscribe(this);
                        break;
                    } else {
                        sendMessage("Неверные логин и пароль");
                        System.out.print("Неверные логин и пароль");

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void sendMessage(String message) {
        try {
            System.out.println("SERVER: Send message to " + nick);
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMessages() {
        try {
            while (true) {
                final String msg = in.readUTF();
                System.out.println("Receive message: " + msg);
                if (msg.startsWith("/")) {
                    if ("/end".equals(msg)) {
                        server.broadcast(nick + ": покинул чат");
                        break;
                    }
                    if (msg.startsWith("/w ")) {
                        String[] tokens = msg.split("\\s");
                        String nick = tokens[1];
                        String str = msg.substring(4 + nick.length());
                        server.sendMsgToClient(this, nick, str);
                    }
                    continue;
                }
                server.broadcast(nick + ": " + msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNick() {
        return nick;
    }
}
