package com.artqqwr.socketchat;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.artqqwr.socketchat.chat.Client;
import com.artqqwr.socketchat.chat.Server;
import com.artqqwr.socketchat.message.CommandMessage;
import com.artqqwr.socketchat.message.TextMessage;

public class Main {
    public static void main(String[] args) {
        try (BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Choose mode:");
            System.out.println("1 - Start server");
            System.out.println("2 - Start client");
            System.out.print("Your choice: ");
            String choice = console.readLine().trim();

            if ("1".equals(choice)) {
                System.out.print("Enter port for the server (default: 3001): ");
                String portInput = console.readLine().trim();
                int port;
                if (portInput.isEmpty()) {
                    port = 3001;
                } else {
                    try {
                        port = Integer.parseInt(portInput);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid port number. Using default port 3001.");
                        port = 3001;
                    }
                }
                System.out.println("Starting server on port " + port + "...");
                Server server = new Server(port);
                server.startServer();
            } else if ("2".equals(choice)) {
                Client client = new Client("localhost", 3001);
                client.connect();

                System.out.println("Choose action:");
                System.out.println("1 - Login");
                System.out.println("2 - Register");
                System.out.print("Your choice: ");
                String action = console.readLine().trim();

                String username;
                if ("1".equals(action)) {
                    System.out.print("Enter your username: ");
                    username = console.readLine();
                    System.out.print("Enter your password: ");
                    String password = console.readLine();
                    client.sendMessage(new CommandMessage(username, "login", password, "logging in"));
                } else if ("2".equals(action)) {
                    System.out.print("Enter desired username: ");
                    username = console.readLine();
                    System.out.print("Enter password: ");
                    String password = console.readLine();
                    System.out.print("Confirm password: ");
                    String confirm = console.readLine();
                    client.sendMessage(new CommandMessage(username, "register", password, confirm));
                } else {
                    System.out.println("Invalid action. Exiting.");
                    client.disconnect();
                    return;
                }

                System.out.println("After successful authentication you can type messages. Type '/exit' to quit.");
                String userInput;
                
                while ((userInput = console.readLine()) != null) {
                    client.sendMessage(new TextMessage(username, userInput));
                    if ("/exit".equalsIgnoreCase(userInput.trim())) {
                        break;
                    }
                }
                client.disconnect();
            } else {
                System.out.println("Invalid choice. Exiting.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
