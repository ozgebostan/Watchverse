package Model;

/**
 * Singleton class that manages the current logged-in user's state.
 */
public class ClientUserSession {

    private static ClientUserSession instance;
    private String username;

    // Private constructor prevents instantiation from other classes
    private ClientUserSession() {
    }

    /**
     * Returns the single instance of UserSession.
     * Synchronized to ensure thread safety during initialization.
     */
    public static synchronized ClientUserSession getInstance() {
        if (instance == null) {
            instance = new ClientUserSession();
        }
        return instance;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Checks if a user is currently logged in.
     */
    public boolean isLoggedIn() {
        return username != null;
    }

    /**
     * Resets the session after logout.
     */
    public void logout() {
        this.username = null;
    }
}