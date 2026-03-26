package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manager class that handles database connection,
 * schema initialization, and table creation.
 */
public class DatabaseManager {

    private static final String DB_FILE = "watchverse.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;

    private Connection connection;
    private static DatabaseManager instance;

    public DatabaseManager() {
        initializeDataBase();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    //Creates the database if it doesn't exist and proceeds to initialize tables.
    private void initializeDataBase() {

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);

            createTables();
            System.out.println("Database connected and tables are ready");
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    //Creates all necessary tables with Foreign Key constraints and Indexes.
    private void createTables() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON;");

            // Users table
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS users(
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT UNIQUE NOT NULL,
                        security_question TEXT NOT NULL,
                        security_answer TEXT NOT NULL,
                        password TEXT NOT NULL)
                    """);

            // Personal Watchlist table
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS watchlists(
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        visibility TEXT DEFAULT 'private' CHECK(visibility IN ('private', 'link_only', 'public')),
                        share_token TEXT,
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                    )
                    """);

            // Social groups
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS user_groups(
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        owner_id INTEGER NOT NULL,
                        groupName TEXT NOT NULL,
                        join_code TEXT UNIQUE,
                        FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
                    )
                    """);

            // Users in groups
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS group_members(
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        group_id INTEGER NOT NULL,
                        user_id INTEGER NOT NULL,
                        FOREIGN KEY (group_id) REFERENCES user_groups(id) ON DELETE CASCADE,
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                        UNIQUE(group_id, user_id)
                    )
                    """);

            // Group Watchlists
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS group_watchlists(
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        group_id INTEGER NOT NULL,
                        watchlist_id INTEGER NOT NULL,
                        added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (group_id) REFERENCES user_groups(id) ON DELETE CASCADE,
                        FOREIGN KEY (watchlist_id) REFERENCES watchlists(id) ON DELETE CASCADE,
                        UNIQUE(group_id, watchlist_id)
                    )
                    """);

            // List Items
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS list_items(
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        watchlist_id INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        content_type TEXT DEFAULT 'MOVIE' CHECK(content_type IN ('MOVIE','SERIES','TV','SHOW')),
                        genres TEXT,
                        api_id TEXT,
                        poster_url TEXT,
                        priority INTEGER DEFAULT 1, -- 1: Low, 2: Medium, 3: High
                        duration INTEGER DEFAULT 0,
                        status TEXT DEFAULT 'PLANNING' CHECK(status IN ('WATCHING', 'FINISHED', 'PLANNING')),
                        current_episode INTEGER DEFAULT 0,
                        total_episodes INTEGER DEFAULT 1,
                        FOREIGN KEY (watchlist_id) REFERENCES watchlists(id) ON DELETE CASCADE,
                        UNIQUE(watchlist_id, api_id)
                    )
                    """);
        }
    }

    //Provides the active database connection to DAO classes.
    public Connection getConnection() {

        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
}