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
import com.artqqwr.socketchat.user.UserDAO;
import com.artqqwr.socketchat.user.UserInfo;

public class ClientHandler implements Runnable {
    private Socket socket;
    private Server server;
    private BufferedReader in;
    private PrintWriter out;

    private String username = "Unknown";
    private String role = "user";
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
            while (!authenticated && (socket.isConnected())) {
                String initJson = in.readLine();
                if (initJson == null) {
                    break;
                }
                Message initMsg = MessageSerializer.deserialize(initJson);
                if (initMsg instanceof CommandMessage) {
                    CommandMessage cmdMsg = (CommandMessage) initMsg;
                    String cmd = cmdMsg.getCommand().toLowerCase();
                    if ("login".equals(cmd)) {
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
                    } else if ("register".equals(cmd)) {
                        if (!cmdMsg.getPassword().equals(cmdMsg.getArgument())) {
                            send(new ErrorMessage("SERVER", "Registration failed: passwords do not match."));
                        } else {
                            if (UserDAO.registerUser(cmdMsg.getSender(), cmdMsg.getPassword())) {
                                username = cmdMsg.getSender();
                                role = "user";
                                authenticated = true;
                                send(new NotificationMessage("SERVER",
                                        "Registration successful. Welcome, " + username + "!"));
                                server.broadcast(new CommandMessage(username, "register", null, "has joined the chat."),
                                        this);
                            } else {
                                send(new ErrorMessage("SERVER", "Registration failed: username already exists."));
                            }
                        }
                    } else {

                        send(new ErrorMessage("SERVER",
                                "Please login or register first using the appropriate command."));
                    }
                } else {
                    send(new ErrorMessage("SERVER", "Invalid input. You must login or register first."));
                }
            }

            if (!authenticated) {
                return;
            }

            String jsonMessage;
            while ((jsonMessage = in.readLine()) != null) {
                Message msg = MessageSerializer.deserialize(jsonMessage);
                if (msg instanceof TextMessage) {
                    TextMessage textMsg = (TextMessage) msg;
                    System.out.println("[" + username + "]: " + textMsg.getText());
                    if ("/exit".equalsIgnoreCase(textMsg.getText().trim())) {
                        send(new NotificationMessage("SERVER", "Disconnecting..."));
                        break;
                    }
                    server.broadcast(textMsg, this);
                } else if (msg instanceof CommandMessage) {
                    CommandMessage cmdMsg = (CommandMessage) msg;
                    if ("kick".equalsIgnoreCase(cmdMsg.getCommand())) {
                        if ("admin".equalsIgnoreCase(role)) {
                            String targetUser = cmdMsg.getArgument();
                            server.kickUser(targetUser);
                        } else {
                            send(new ErrorMessage("SERVER", "You do not have permission for this command."));
                        }
                    } else {
                        System.out.println("Unknown command from " + username + ": " + cmdMsg.getCommand());
                    }
                } else {
                    System.out.println("Unknown message type from " + username);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            server.removeClient(this);
            server.broadcast(new CommandMessage(username, "logout", null, "has logged out."), this);
        }
    }

    public void send(Message message) {
        if (out != null) {
            var json = MessageSerializer.serialize(message);
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
