package com.geekbrains.chat.client;

import com.geekbrains.chat.Command;
import javafx.application.Platform;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


public class ChatClient {

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Thread timerThread;

    private final Controller controller;
    private ChatHistory chatHistory;

    public ChatClient(Controller controller) {
        this.controller = controller;
        timerThread = new Thread(() -> {
            try {
                Thread.sleep(120000);
                System.out.println("Время авторизации вышло");
                Platform.exit();
                sendMessage(Command.END);
            } catch (InterruptedException e) {
                System.out.println("Успешная авторизация");
            }
        });
        timerThread.start();
    }

    public void openConnection() throws Exception {
        socket = new Socket("localhost", 8189);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        chatHistory = new ChatHistory();
        final Thread readThread = new Thread(() -> {
            try {
                waitAuthenticate();
                readMessage();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnection();
            }
        });
        readThread.setDaemon(true);
        readThread.start();

    }

    private void readMessage() throws IOException {
        while (true) {
            final String message = in.readUTF();
            System.out.println("Receive message: " + message);
            if (Command.isCommand(message)) {
                Command command = Command.getCommand(message);
                String[] params = command.parse(message);
                if (command == Command.END) {
                    controller.setAuth(false);
                    break;
                }
                if (command == Command.ERROR) {
                    Platform.runLater(() -> controller.showError(params));
                    continue;
                }
                if (command == Command.CLIENTS) {
                    controller.updateClientList(params);
                    continue;
                }
            }
            controller.addMessage(message);
            chatHistory.saveHistory(message);
        }
    }

    private void waitAuthenticate() throws IOException {
        while (true) {
            final String msgAuth = in.readUTF();
            if (Command.isCommand(msgAuth)) {
                Command command = Command.getCommand(msgAuth);
                String[] params = command.parse(msgAuth);
                if (command == Command.AUTHOK) {
                    timerThread.interrupt();
                    final String nick = params[0];
                    controller.addMessage(chatHistory.loadHistory());
                    controller.addMessage("Успешная авторизация под ником " + nick);
                    controller.setAuth(true);
                    break;
                }
                if (command == Command.ERROR) {
                    Platform.runLater(() -> controller.showError(params));
                }
            }
        }
    }

    private void closeConnection() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }

    public void sendMessage(String message) {
        try {
            System.out.println("Send message: " + message);
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Command command, String... params) {
        sendMessage(command.collectMessage(params));
    }
}
