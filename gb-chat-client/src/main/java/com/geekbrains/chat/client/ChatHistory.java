package com.geekbrains.chat.client;

import com.geekbrains.chat.Command;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatHistory {

    public void saveHistory(String message) {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        if (Command.isCommand(message)) {
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("gb-chat-client/chatHistory.txt", true))) {
            writer.write("[" + formatter.format(date) + "] " + message + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String loadHistory() {
        File file = new File("gb-chat-client/chatHistory.txt");

        int lines = 100;
        int readLines = 0;
        StringBuilder builder = new StringBuilder();
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "r");
            long fileLength = file.length() - 1;
            randomAccessFile.seek(fileLength);
            for (long pointer = fileLength; pointer >= 0; pointer--) {
                randomAccessFile.seek(pointer);
                char ch = (char)randomAccessFile.read();
                if (ch == '\n') {
                    readLines++;
                    if (readLines > lines)
                        break;
                }
                builder.append(ch);
            }
            builder.reverse();
            String historyLog = new String(builder.toString().getBytes("ISO-8859-1"), "UTF-8");
            return historyLog.trim();
        } catch (IOException e) {
            System.out.println("Файл истории не найден. Создаем новый!");
        } finally {
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }
}
