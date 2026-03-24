package Server;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * Main Entry point for the Watchverse Server.
 * Listens for client connections and delegates them to ClientHandlers.
 */
public class WatchverseServer {

    private static final int PORT = 12345;

    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack", "true");

        System.out.println("--- Watchverse Server Starting ---");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is successfully listening on port: " + PORT);
            System.out.println("Waiting for clients...");

            // Server will wait for clients indefinitely
            while (true) {
                try {
                    // Accept when there is a request from a client
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                    // Thread to take care of the client
                    ClientHandler handler = new ClientHandler(clientSocket);
                    Thread clientThread = new Thread(handler);
                    clientThread.start();

                } catch (Exception clientException) {
                    System.err.println("Error accepting client connection: " + clientException.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Critical Server Error: Could not start the server on port " + PORT);
            e.printStackTrace();
        }
    }
}