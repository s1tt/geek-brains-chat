package com.geekbrains.chat.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class ChatController implements Initializable {
    @FXML
    TextArea chatArea;

    @FXML
    TextField messageField;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            Thread readThread = new Thread(() -> {
                    try {
                        while (true) {
                            String inputMessage = in.readUTF();
                            if ("/end".equalsIgnoreCase(inputMessage)) {
                                Platform.exit();
                                break;
                            }
                            chatArea.appendText(inputMessage + "\n");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            });
            readThread.start();
        } catch (IOException e) {
            System.out.println("Невозможно подключиться к серверу");
            System.exit(0);
        }
    }

    public void sendMessage() {
        try {
            out.writeUTF(messageField.getText());
            messageField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}