package com.artqqwr.socketchat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.artqqwr.socketchat.chat.Client;
import com.artqqwr.socketchat.chat.Server;

public class Main {

    private static final int DEFAULT_PORT = 3001;

    public static void main(String[] args) {
        try (BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {
            String choice = getInput(console, "Choose mode:\n1 - Start server\n2 - Start client\nYour choice: ");
            switch (choice) {
                case "1" -> startServer(console);
                case "2" -> startClient(console);
                default -> System.out.println("Invalid choice. Exiting.");
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void startServer(BufferedReader console) throws IOException {
        String portInput = getInput(console, "Enter port for the server (default: 3001): ");
        int port = parsePort(portInput, DEFAULT_PORT);
        System.out.println("Starting server on port " + port + "...");
        new Server(port).startServer();
    }

    private static void startClient(BufferedReader console) throws IOException {
        Client client = new Client("localhost", DEFAULT_PORT);
        client.connect();

        String action = getInput(console, "Choose action:\n1 - Login\n2 - Register\nYour choice: ");
        switch (action) {
            case "1" -> handleLogin(console, client);
            case "2" -> handleRegister(console, client);
            default -> {
                System.out.println("Invalid action. Exiting.");
                client.disconnect();
            }
        }

        processUserMessages(console, client);
        client.disconnect();
    }

    private static void handleLogin(BufferedReader console, Client client) throws IOException {
        String username = getInput(console, "Enter your username: ");
        String password = getInput(console, "Enter your password: ");

        client.setUsername(username);

        client.sendCommandMessage("login", password, "logging in");
    }

    private static void handleRegister(BufferedReader console, Client client) throws IOException {
        String username = getInput(console, "Enter desired username: ");
        String password = getInput(console, "Enter password: ");
        String confirm = getInput(console, "Confirm password: ");

        client.setUsername(username);
        
        client.sendCommandMessage("register", password, confirm);
    }

    private static void processUserMessages(BufferedReader console, Client client) throws IOException {
        System.out.println("After successful authentication, type messages. Type '/exit' to quit.");
        String userInput;
        while ((userInput = console.readLine()) != null) {
            client.sendTextMessage(userInput);
            if ("/exit".equalsIgnoreCase(userInput.trim())) {
                break;
            }
        }
    }

    private static int parsePort(String portInput, int defaultPort) {
        if (portInput.isEmpty()) {
            return defaultPort;
        }
        try {
            return Integer.parseInt(portInput);
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number. Using default port " + defaultPort + ".");
            return defaultPort;
        }
    }

    private static String getInput(BufferedReader console, String prompt) throws IOException {
        System.out.print(prompt);
        return console.readLine().trim();
    }
}
