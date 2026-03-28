package Server;

import Model.AuthResult;
import Model.Item;
import Server.Services.ApiManager;
import Server.Services.AuthService;
import Server.Services.WatchlistService;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class ClientHandler implements Runnable {

    private final Socket socket;
    private final ApiManager apiManager;
    private final AuthService authService;
    private final WatchlistService watchlistService;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.apiManager = new ApiManager();
        this.authService = new AuthService();
        this.watchlistService = new WatchlistService();
    }

    @Override
    public void run() {

        // ObjectOutputStream first
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            Object receivedMessage;
            while ((receivedMessage = in.readObject()) != null) {
                if (!(receivedMessage instanceof String request)) continue;

                String[] parts = request.split("###");
                String command = parts[0];

                switch (command) {
                    case "SEARCH" -> handleSearch(out, parts);
                    case "LOGIN" -> handleLogin(out, parts);
                    case "REGISTER" -> handleRegister(out, parts);
                    case "ADD_ITEM" -> handleAddItem(out, parts);
                    case "GET_GROUPS" -> sendResponse(out, new ArrayList<String>());
                    case "GET_GROUP_ITEMS" -> sendResponse(out, new ArrayList<Item>());
                    case "GET_LIST_ITEMS" -> handleGetItems(out, parts);
                    case "REMOVE_ITEM" -> handleRemoveItem(out, parts);
                    case "CREATE_LIST" -> handleCreateList(out, parts);
                    case "GET_WATCHLISTS" -> handleGetMyLists(out, parts);
                    case "DELETE_LIST" -> handleDeleteList(out, parts);
                    case "DELETE_ACCOUNT" -> {
                        if (parts.length >= 3) {
                            sendResponse(out, authService.deleteAccount(parts[1], parts[2]).toString());
                        } else {
                            sendResponse(out, "ERROR: Missing parameters");
                        }
                    }
                    case "CREATE_GROUP" -> handleCreateGroup(out, parts);
                    case "GET_GROUP_CODE" -> handleGetGroupCode(out, parts);
                    case "GET_MY_GROUPS" -> handleGetUserGroups(out, parts);
                    case "JOIN_GROUP" -> handleJoinGroup(out, parts);
                    case "DELETE_GROUP" -> handleDeleteGroup(out, parts);
                    case "GET_PUBLIC_LISTS" -> sendResponse(out, watchlistService.getPublicWatchlists());
                    case "CHANGE_PASSWORD" -> {
                        if (parts.length >= 4) {
                            sendResponse(out, authService.forgotPassword(parts[1], parts[3]).toString());
                        } else {
                            sendResponse(out, "ERROR: Missing parameters");
                        }
                    }
                    case "CHECK_USER" -> sendResponse(out, authService.isUserExists(parts[1]) ? "EXISTS" : "NOT_FOUND");
                    case "GET_QUESTION" -> sendResponse(out, authService.getSecurityQuestion(parts[1]));
                    case "VERIFY_ANSWER" -> sendResponse(out, authService.verifySecurityAnswer(parts[1], parts[2]).toString());
                    case "RESET_PASSWORD" -> {
                        if (parts.length >= 3) {
                            AuthResult result = authService.forgotPassword(parts[1], parts[2]);
                            sendResponse(out, result.toString());
                        } else {
                            sendResponse(out, AuthResult.ERROR.toString());
                        }
                    }

                    default -> {
                        System.out.println("Unknown command: " + command);
                        sendResponse(out, new ArrayList<>());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Server connection exception: " + e.getMessage());
        } finally {
            closeSocket();
        }
    }

    private void handleAddItem(ObjectOutputStream out, String[] parts) throws Exception {
        if (parts.length >= 8) {
            String user = parts[1];
            String list = parts[2];
            String title = parts[3];
            String type = parts[4];
            String genres = parts[5];
            String apiId = parts[6];
            String posterUrl = "null".equals(parts[7]) ? null : parts[7];

            int priority = 1;
            int duration = 0;

            if (parts.length >= 10) {
                try {
                    priority = Integer.parseInt(parts[8]);
                    duration = Integer.parseInt(parts[9]);
                } catch (NumberFormatException ignored) {}
            }

            Item newItem = new Item(title, type, genres, apiId, posterUrl, priority, duration);
            sendResponse(out, watchlistService.addItem(user, list, newItem));
        }
    }

    private void handleSearch(ObjectOutputStream out, String[] parts) {
        try {
            System.out.println("[SERVER] Search begin: " + parts[1]);

            String title = parts[1];
            String type = (parts.length >= 3) ? parts[2] : "movie";

            String jsonResponse = apiManager.search(title, type);

            if (jsonResponse == null) {
                System.err.println("[SERVER] ERROR: TMDB returned null!");
                sendResponse(out, new ArrayList<Item>());
                return;
            }

            List<Item> results = apiManager.parseResponse(jsonResponse, type);

            System.out.println("[SERVER] Operation completed, " + results.size() + " movies/series sent.");
            sendResponse(out, results);

        } catch (Exception e) {
            System.err.println("[SERVER] Unexpected critical error in handleSearch!");
            e.printStackTrace();
            try { sendResponse(out, new ArrayList<Item>()); } catch (Exception ignored) {}
        }
    }

    private void handleLogin(ObjectOutputStream out, String[] parts) throws Exception {
        if (parts.length >= 3) {
            sendResponse(out, authService.login(parts[1], parts[2]).toString());
        }
    }

    private void handleRegister(ObjectOutputStream out, String[] parts) throws Exception {
        if (parts.length >= 5) {
            sendResponse(out, authService.register(parts[1], parts[2], parts[3], parts[4]).toString());
        }
    }

    private void handleGetItems(ObjectOutputStream out, String[] parts) throws Exception {
        if (parts.length >= 3) {
            sendResponse(out, watchlistService.getListItems(parts[1], parts[2]));
        }
    }

    private void handleRemoveItem(ObjectOutputStream out, String[] parts) throws Exception {
        if (parts.length >= 4) {
            sendResponse(out, watchlistService.removeItem(parts[1], parts[2], parts[3]) ? "SUCCESS" : "FAIL");
        }
    }

    private void handleCreateList(ObjectOutputStream out, String[] parts) throws Exception {
        if (parts.length >= 4) {
            sendResponse(out, watchlistService.createWatchlist(parts[1], parts[2], parts[3]) ? "SUCCESS" : "FAIL");
        }
    }

    private void handleGetMyLists(ObjectOutputStream out, String[] parts) throws Exception {
        if (parts.length >= 2) {
            sendResponse(out, watchlistService.getUserWatchlists(parts[1]));
        }
    }

    private void handleJoinGroup(ObjectOutputStream out, String[] parts) throws Exception {
        if (parts.length >= 3) {
            sendResponse(out, watchlistService.joinGroup(parts[1], parts[2]));
        }
    }

    private void handleCreateGroup(ObjectOutputStream out, String[] parts) throws Exception {
        if (parts.length >= 3) {
            sendResponse(out, watchlistService.createGroup(parts[1], parts[2]) ? "SUCCESS" : "FAIL");
        }
    }

    private void handleGetUserGroups(ObjectOutputStream out, String[] parts) throws Exception {
        if (parts.length >= 2) {
            sendResponse(out, watchlistService.getUserGroups(parts[1]));
        }
    }

    private void handleDeleteGroup(ObjectOutputStream out, String[] parts) throws Exception {
        if (parts.length >= 3) {
            sendResponse(out, watchlistService.deleteGroup(parts[1], parts[2]) ? "SUCCESS" : "FAIL");
        }
    }

    private void handleGetGroupCode(ObjectOutputStream out, String[] parts) throws Exception {
        if (parts.length >= 3) {
            sendResponse(out, watchlistService.getGroupCode(parts[1], parts[2]));
        }
    }

    private void handleDeleteList(ObjectOutputStream out, String[] parts) throws Exception {
        if (parts.length >= 3) {
            String username = parts[1];
            String listName = parts[2];
            boolean success = watchlistService.deleteWatchlist(username, listName);
            sendResponse(out, success ? "SUCCESS" : "FAIL");
        }
    }


    private void sendResponse(ObjectOutputStream out, Object obj) throws Exception {
        out.writeObject(obj);
        out.flush();
        out.reset();
    }

    private void closeSocket() {
        try {
            if (socket != null && !socket.isClosed()) {
                System.out.println("Connection closed: " + socket.getInetAddress());
                socket.close();
            }
        } catch (Exception ignored) {}
    }
}