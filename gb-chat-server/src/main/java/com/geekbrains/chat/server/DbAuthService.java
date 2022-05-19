package com.geekbrains.chat.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class DbAuthService implements AuthService {

    private Connection connection;
    private Statement stm;
    private static final Logger LOGGER = LogManager.getLogger(DbAuthService.class);

    public DbAuthService() {
        run();
    }

    @Override
    public String getNickByLoginAndPassword(String login, String password) {
        try (PreparedStatement prStmt = connection.prepareStatement("select nick from users where login = ? and password = ?")) {
            prStmt.setString(1, login);
            prStmt.setString(2, password);
            try (ResultSet rs = prStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.error(e);
        }
        return null;
    }

    @Override
    public void run() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:gb-chat-server/clients.db");
            stm = connection.createStatement();
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
            stm.close();
        } catch (ClassNotFoundException | SQLException e) {
            LOGGER.fatal("Unable to connect to DB", e);
        }
    }

    @Override
    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            LOGGER.error("Error closing database connection", e);
        }
    }
}
