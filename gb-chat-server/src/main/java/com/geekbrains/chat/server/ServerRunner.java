package com.geekbrains.chat.server;

import java.io.IOException;

public class ServerRunner {

    public static void main(String[] args) throws IOException {
        new ChatServer().run();
    }
}
