package Client.Network;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Singleton class to manage the connection between Client and Server.
 */
public class SocketManager {
    private static SocketManager instance;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private final String HOST = "localhost";
    private final int PORT = 567;


    public static synchronized SocketManager getInstance() {
        if (instance == null || instance.socket == null || instance.socket.isClosed()) {
            instance = new SocketManager();
        }
        return instance;
    }

    private SocketManager() {
        try {
            this.socket = new Socket(HOST, PORT);
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.out.flush();

            this.in = new ObjectInputStream(socket.getInputStream());

            System.out.println("Connection Successful!");
        } catch (Exception e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }


    /**
     * Sends a request to the server and waits for the response.
     */
    public synchronized Object sendRequest(String request) {
        if (out == null || in == null) {
            System.err.println("[SocketManager] Cannot send the request due to connection error.");
            return null;
        }

        try {
            out.writeObject(request);
            out.flush();
            return in.readObject();
        } catch (Exception e) {
            System.err.println("[SocketManager] Connection error: " + e.getMessage());
            closeConnection();
            return null;
        }
    }

    public void closeConnection() {
        try {
            if (socket != null) socket.close();
            instance = null;
        } catch (Exception ignored) {}
    }
}