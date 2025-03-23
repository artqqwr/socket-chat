package com.artqqwr.socketchat.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.artqqwr.socketchat.message.CommandMessage;
import com.artqqwr.socketchat.message.Message;
import com.artqqwr.socketchat.message.NotificationMessage;

public class Server {
    private int port;
    private Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();
    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    public Server(int port) {
        this.port = port;
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection from " + clientSocket);
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler);
                executorService.submit(handler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
    }

    // Broadcast message to all clients except the sender (if provided)
    public void broadcast(Message message, ClientHandler exclude) {
        // String json = MessageSerializer.serialize(message);
        for (ClientHandler client : clients) {
            if (client != exclude) {
                client.send(message);
            }
        }
    }

    public void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        System.out.println("Client disconnected: " + clientHandler.getSocket());
    }
    
    public void kickUser(String targetUsername) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equalsIgnoreCase(targetUsername)) {
                client.send(new NotificationMessage("SERVER", "You have been kicked by an admin."));
                client.disconnect();
                clients.remove(client);
                broadcast(new CommandMessage("SYSTEM", "kick", null, targetUsername + " has been kicked."), null);
                System.out.println("User " + targetUsername + " was kicked.");
                break;
            }
        }
    }
}
