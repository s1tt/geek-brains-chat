package com.geekbrains.chat.server;

import com.geekbrains.chat.Command;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler {
    private final Socket socket;
    private final ChatServer server;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final AuthService authService;

    private String nick;
    private static final Logger LOGGER = LogManager.getLogger(ClientHandler.class);

    public ClientHandler(Socket socket, ChatServer server, AuthService authService) {
        try {
            this.nick = "";
            this.socket = socket;
            this.server = server;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.authService = authService;
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {
                try {
                    authenticate();
                    readMessages();
                } finally {
                    closeConnection();
                }
            });
            executorService.shutdown();
        } catch (IOException e) {
            LOGGER.fatal(e);
            throw new RuntimeException();
        }
    }

    private void closeConnection() {
        sendMessage(Command.END);
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            LOGGER.error(e);
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            LOGGER.error(e);
        }
        try {
            if (socket != null) {
                server.unsubscribe(this);
                socket.close();
            }
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }

    private void authenticate() {
        while (true) {
            try {
                final String str = in.readUTF();
                if (Command.isCommand(str)) {
                    final Command command = Command.getCommand(str);
                    final String[] params = command.parse(str);
                    if (command == Command.AUTH) {
                        final String login = params[0];
                        final String password = params[1];
                        final String nick = authService.getNickByLoginAndPassword(login, password);
                        if (nick != null) {
                            if (server.isNickBusy(nick)) {
                                sendMessage(Command.ERROR, "The user is already logged in");
                                continue;
                            }
                            sendMessage(Command.AUTHOK, nick);
                            this.nick = nick;
                            server.broadcast("The user " + nick + " has entered to the chat");
                            server.subscribe(this);
                            break;
                        } else {
                            sendMessage(Command.ERROR, "Invalid username or password");
                        }
                    }
                }
            } catch (IOException | SQLException e) {
                LOGGER.error(e);
                break;
            }
        }
    }

    public void sendMessage(Command command, String... params) {
        sendMessage(command.collectMessage(params));
    }

    public void sendMessage(String message) {
        try {
            LOGGER.debug("SERVER: Send message: " + message);
            out.writeUTF(message);
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }

    private void readMessages() {
        try {
            while (true) {
                final String msg = in.readUTF();
                LOGGER.debug("Receive message: " + msg);
                if (Command.isCommand(msg)) {
                    Command command = Command.getCommand(msg);
                    String[] params = command.parse(msg);
                    if (command == Command.END) {
                        server.broadcast(nick + ": left the chat");
                        break;
                    }
                    if (command == Command.PRIVATE_MESSAGE) {
                        server.sendMsgToClient(this, params[0], params[1]);
                        continue;
                    }
                }
                server.broadcast(nick + ": " + msg);
            }
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }

    public String getNick() {
        return nick;
    }
}
