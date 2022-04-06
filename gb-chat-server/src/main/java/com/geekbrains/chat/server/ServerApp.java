package com.geekbrains.chat.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ServerApp {
    public static void main(String[] args) {
        try(ServerSocket serverSocket = new ServerSocket(8189)) {
            System.out.println("Сервер запущен. Ожидаем клиентов...");

            Socket socket = serverSocket.accept();
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            Thread readThread = new Thread(() -> {
                try {
                    Scanner s = new Scanner(System.in);
                    while (true) {
                        out.writeUTF("Сообщение от сервера: " + s.nextLine());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            readThread.start();

            System.out.println("Новый клиент подключился");
            while (true) {
                String inputMessage = in.readUTF();
                System.out.println("Сообщение от клиента: " + inputMessage);
                if ("/end".equalsIgnoreCase(inputMessage)) {
                    out.writeUTF("/end");
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
