package com.geekbrains.chat.server;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerApp {
    public static void main(String[] args) {
        try(ServerSocket serverSocket = new ServerSocket(8189)) {
            System.out.println("Сервер запущен. Ожидаем клиентов...");

            Socket socket = serverSocket.accept();
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            System.out.println("Новый клиент подключился");
            while (true) {
                String inputMessage = in.readUTF();
                System.out.println(inputMessage);
                if ("/end".equalsIgnoreCase(inputMessage)) {
                    break;
                }
                out.writeUTF("ECHO: " + inputMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Сервер остановлен");
    }
}
