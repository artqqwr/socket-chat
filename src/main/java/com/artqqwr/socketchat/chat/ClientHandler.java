package com.artqqwr.socketchat.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.artqqwr.socketchat.message.CommandMessage;
import com.artqqwr.socketchat.message.ErrorMessage;
import com.artqqwr.socketchat.message.Message;
import com.artqqwr.socketchat.message.MessageSerializer;
import com.artqqwr.socketchat.message.NotificationMessage;
import com.artqqwr.socketchat.message.TextMessage;
import com.artqqwr.socketchat.user.Role;
import com.artqqwr.socketchat.user.UserDAO;
import com.artqqwr.socketchat.user.UserInfo;

public class ClientHandler implements Runnable {
    private Socket socket;
    private final Server server;
    private BufferedReader in;
    private PrintWriter out;

    private String username = "Unknown";
    private Role role = Role.USER;
    private boolean authenticated = false;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public void run() {
        try {
            if (!handleAuthentication()) {
                return;
            }
            handleMessages();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    private boolean handleAuthentication() throws IOException {
        while (!authenticated && socket.isConnected()) {
            String initJson = in.readLine();
            if (initJson == null) {
                break;
            }
            Message initMsg = MessageSerializer.deserialize(initJson);
            switch (initMsg) {
                case CommandMessage cmdMsg -> {
                    String command = cmdMsg.getCommand().toLowerCase();
                    switch (command) {
                        case "login" -> processLogin(cmdMsg);
                        case "register" -> processRegister(cmdMsg);
                        default -> send(new ErrorMessage("SERVER", 
                            "Please login or register first using the appropriate command."));
                    }
                }
                default -> send(new ErrorMessage("SERVER", 
                    "Invalid input. You must login or register first."));
            }
        }
        return authenticated;
    }

    private void processLogin(CommandMessage cmdMsg) {
        UserInfo user = UserDAO.validateUser(cmdMsg.getSender(), cmdMsg.getPassword());
        if (user != null) {
            username = user.getUsername();
            role = user.getRole();
            authenticated = true;
            send(new NotificationMessage("SERVER", "Login successful. Welcome, " + username + "!"));
            server.broadcast(new CommandMessage(username, "login", null, "has logged in."), this);
        } else {
            send(new ErrorMessage("SERVER", "Login failed. Please try again."));
        }
    }

    private void processRegister(CommandMessage cmdMsg) {
        if (!cmdMsg.getPassword().equals(cmdMsg.getArgument())) {
            send(new ErrorMessage("SERVER", "Registration failed: passwords do not match."));
        } else if (UserDAO.registerUser(cmdMsg.getSender(), cmdMsg.getPassword())) {
            username = cmdMsg.getSender();
            role = Role.USER; // При регистрации по умолчанию присваиваем роль USER
            authenticated = true;
            send(new NotificationMessage("SERVER", "Registration successful. Welcome, " + username + "!"));
            server.broadcast(new CommandMessage(username, "register", null, "has joined the chat."), this);
        } else {
            send(new ErrorMessage("SERVER", "Registration failed: username already exists."));
        }
    }

    private void handleMessages() throws IOException {
        String jsonMessage;
        while ((jsonMessage = in.readLine()) != null) {
            Message msg = MessageSerializer.deserialize(jsonMessage);
            switch (msg) {
                case TextMessage textMsg -> processTextMessage(textMsg);
                case CommandMessage cmdMsg -> processCommand(cmdMsg);
                default -> System.out.println("Unknown message type from " + username);
            }
        }
    }

    private void processTextMessage(TextMessage textMsg) {
        String text = textMsg.getText();
        System.out.println("[" + username + "]: " + text);
        if ("/exit".equalsIgnoreCase(text.trim())) {
            send(new NotificationMessage("SERVER", "Disconnecting..."));
        } else {
            server.broadcast(textMsg, this);
        }
    }

    private void processCommand(CommandMessage cmdMsg) {
        switch (cmdMsg.getCommand().toLowerCase()) {
            case "kick" -> {
                if (role == Role.ADMIN) {
                    server.kickUser(cmdMsg.getArgument());
                } else {
                    send(new ErrorMessage("SERVER", "You do not have permission for this command."));
                }
            }
            default -> System.out.println("Unknown command from " + username + ": " + cmdMsg.getCommand());
        }
    }

    private void cleanup() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.removeClient(this);
        server.broadcast(new CommandMessage(username, "logout", null, "has logged out."), this);
    }

    public void send(Message message) {
        if (out != null) {
            String json = MessageSerializer.serialize(message);
            out.println(json);
        }
    }

    public void disconnect() {
        try {
            if (socket != null)
                socket.close();
            if (in != null)
                in.close();
            if (out != null)
                out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
