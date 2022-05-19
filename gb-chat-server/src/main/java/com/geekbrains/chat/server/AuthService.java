package com.geekbrains.chat.server;

import java.io.Closeable;
import java.io.IOException;
import java.sql.SQLException;

public interface AuthService extends Closeable {
    String getNickByLoginAndPassword(String login, String password) throws SQLException;

    void run();

    @Override
    void close() throws IOException;
}
