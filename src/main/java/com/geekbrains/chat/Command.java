package com.geekbrains.chat;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Command {

    AUTH("/auth") {
        @Override
        public String[] parse(String commandText) {
            String[] split = commandText.split(COMMAND_DELIMITER);
            return new String[] {split[1], split[2]};
        }
    },
    AUTHOK("/authok") {
        @Override
        public String[] parse(String commandText) {
            return new String[]{commandText.split(COMMAND_DELIMITER)[1]};
        }
    },
    PRIVATE_MESSAGE("/w") {
        @Override
        public String[] parse(String commandText) {
            String[] split = commandText.split(COMMAND_DELIMITER, 3);
            return new String[] {split[1], split[2]};
        }
    },
    END("/end") {
        @Override
        public String[] parse(String commandText) {
            return new String[0];
        }
    },
    ERROR("/error") {
        @Override
        public String[] parse(String commandText) {
            String errorMsg = commandText.split(COMMAND_DELIMITER, 2)[1];
            return new String[]{errorMsg};
        }
    },
    CLIENTS("/clients") {
        @Override
        public String[] parse(String commandText) {
            String[] split = commandText.split(COMMAND_DELIMITER);
            String[] nicks = new String[split.length - 1];
            System.arraycopy(split, 1, nicks, 0, split.length - 1);
            return nicks;
        }
    };
    private static final Map<String, Command> map = Stream.of(Command.values()).collect(Collectors.toMap(Command::getCommand, Function.identity()));

    private String command;
    private String[] params = new String[0];
    private static final String COMMAND_DELIMITER = "\\s+";

    public String getCommand() {
        return command;
    }

    Command(String command) {
        this.command = command;
    }

    public static boolean isCommand(String message) {
        return message.startsWith("/");
    }

    public String[] getParams() {
        return params;
    }

    public static Command getCommand (String message) {
        message = message.trim();
        if (!isCommand(message)) {
            throw new RuntimeException("'" + message + "' is not command");
        }
        int index = message.indexOf(" ");
        String cmd = index > 0 ? message.substring(0, index) : message;

        Command command = map.get(cmd);
        if (command == null) {
            throw new RuntimeException("'" + cmd + "' unknown command");
        }
        return command;
    }

    public abstract String[] parse(String commandText);

    public String collectMessage(String... params) {
        String command = this.getCommand();
        return command +
                (params == null
                        ? ""
                        : " " + String.join(" ", params));
    }
}
