package com.geekbrains.chat.server;

import java.io.IOException;
import java.sql.*;

public class DbAuthService implements AuthService {

    private static Connection connection;
    private static PreparedStatement prStmt;
    private static Statement stm;

    public DbAuthService() {
        run();
    }

    @Override
    public String getNickByLoginAndPassword(String login, String password) {
        try {
            stm.executeUpdate("create table if not exists users (" +
                    "id integer primary key autoincrement," +
                    "login text not null," +
                    "password text not null," +
                    "nick text not null," +
                    "unique (login, password, nick))");
            stm.executeUpdate("insert or ignore into users (login, password, nick) " +
                    "values ('admin1', '123', 'Alex'), " +
                            "('admin2', '123', 'Bob'), " +
                            "('admin3', '123', 'Max')");

            prStmt = connection.prepareStatement("select nick from users where login = ? and password = ?");
            prStmt.setString(1, login);
            prStmt.setString(2, password);
            try (ResultSet rs = prStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void run() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:gb-chat-server/clients.db");
            stm = connection.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Невозможно подключиться к БД");
        }
    }

    @Override
    public void close() throws IOException {
        try {
            if (stm != null) {
                stm.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (prStmt != null) {
                prStmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
