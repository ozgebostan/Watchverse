package Database.daos;

import Database.DatabaseManager;
import Model.Item;
import Model.PublicWatchlist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton DAO class for managing watchlists, groups, and media items.
 */
public class WatchlistDAO {

    private static WatchlistDAO instance;
    private final DatabaseManager dbManager;

    // Private constructor for Singleton
    private WatchlistDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public static synchronized WatchlistDAO getInstance() {
        if (instance == null) {
            instance = new WatchlistDAO();
        }
        return instance;
    }

    // --- WATCHLIST OPERATIONS ---

    public boolean createWatchlist(String username, String listName, String visibility) throws SQLException {
        String sql = "INSERT INTO watchlists (user_id, name, visibility) " +
                "VALUES ((SELECT id FROM users WHERE username = ?), ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, listName);
            pstmt.setString(3, visibility.toLowerCase());
            return pstmt.executeUpdate() == 1;
        }
    }

    public List<String> getUserWatchlists(String username) throws SQLException {
        List<String> watchlists = new ArrayList<>();
        String sql = "SELECT w.name FROM watchlists w JOIN users u ON w.user_id = u.id WHERE u.username = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    watchlists.add(rs.getString("name"));
                }
            }
        }
        return watchlists;
    }

    public String addItemToWatchlist(String username, String listName, Item item) throws SQLException {
        int watchlistId = getWatchlistId(username, listName);
        if (watchlistId == -1) return "ERROR:LIST_NOT_FOUND";

        String sql = "INSERT INTO list_items (watchlist_id, title, content_type, genres, api_id, " +
                "poster_url, priority, duration, release_year, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'PLANNING')";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, watchlistId);
            ps.setString(2, item.title());
            ps.setString(3, normalizeContentType(item.type()));
            ps.setString(4, item.genres());
            ps.setString(5, item.apiId());
            ps.setString(6, item.posterUrl());
            ps.setInt(7, item.priority());
            ps.setInt(8, item.duration());
            ps.setInt(9, item.releaseYear());


            ps.executeUpdate();
            return "SUCCESS";
        } catch (SQLException e) {
            if (e.getErrorCode() == 19 || e.getMessage().contains("UNIQUE")) {
                return "ALREADY_EXISTS";
            }
            throw e;
        }
    }

    public List<Item> getItemsInWatchlist(String username, String listName) throws SQLException {
        List<Item> items = new ArrayList<>();
        int watchlistId = getWatchlistId(username, listName);
        if (watchlistId == -1) return items;

        String sql = "SELECT title, content_type, genres, api_id, poster_url, priority, duration, release_year, added_date " +
                "FROM list_items WHERE watchlist_id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, watchlistId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new Item(
                            rs.getString("title"),
                            rs.getString("content_type"),
                            rs.getString("genres"),
                            rs.getString("api_id"),
                            rs.getString("poster_url"),
                            rs.getInt("priority"),
                            rs.getInt("duration"),
                            rs.getInt("release_year"),
                            rs.getString("added_date")
                    ));
                }
            }
        }
        return items;
    }

    public boolean deleteWatchlist(String username, String listName) throws SQLException {
        int watchlistId = getWatchlistId(username, listName);
        if (watchlistId == -1) return false;

        String sql = "DELETE FROM watchlists WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, watchlistId);
            return ps.executeUpdate() > 0;
        }
    }

    public String getWatchlistVisibility(String username, String listName) throws SQLException {
        String sql = "SELECT visibility FROM watchlists w JOIN users u ON w.user_id = u.id WHERE u.username = ? AND w.name = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, listName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("visibility") : "private";
            }
        }
    }

    // --- GROUP OPERATIONS ---

    public boolean createGroup(String username, String groupName) throws SQLException {
        String joinCode = java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String sql = "INSERT INTO user_groups (owner_id, groupName, join_code) VALUES ((SELECT id FROM users WHERE username = ?), ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, groupName);
            ps.setString(3, joinCode);
            return ps.executeUpdate() == 1;
        }
    }

    public List<String> getUserGroups(String username) throws SQLException {
        List<String> groups = new ArrayList<>();
        // Fetches groups where user is owner OR a member
        String sql = "SELECT groupName FROM user_groups WHERE owner_id = (SELECT id FROM users WHERE username = ?) " +
                "UNION " +
                "SELECT g.groupName FROM user_groups g JOIN group_members gm ON g.id = gm.group_id " +
                "WHERE gm.user_id = (SELECT id FROM users WHERE username = ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    groups.add(rs.getString("groupName"));
                }
            }
        }
        return groups;
    }

    public String joinGroup(String username, String code) throws SQLException {
        String findGroupSql = "SELECT id, groupName FROM user_groups WHERE join_code = ?";
        int groupId;
        String groupName;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(findGroupSql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    groupId = rs.getInt("id");
                    groupName = rs.getString("groupName");
                } else return "NOT_FOUND";
            }

            String insertSql = "INSERT INTO group_members (group_id, user_id) VALUES (?, (SELECT id FROM users WHERE username = ?))";
            try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                psInsert.setInt(1, groupId);
                psInsert.setString(2, username);
                psInsert.executeUpdate();
                return "SUCCESS:" + groupName;
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 19) return "ALREADY_MEMBER";
            throw e;
        }
    }

    public List<PublicWatchlist> getGroupWatchlistObjects(String username, String groupName) throws SQLException {
        List<PublicWatchlist> lists = new ArrayList<>();

        // SQL: Gruba bağlı olan watchlist'lerin sadece ID ve isimlerini getirir.
        // Kullanıcının grup üyesi veya sahibi olup olmadığını da kontrol eder.
        String sql = """
            SELECT w.id, w.name FROM watchlists w
            JOIN group_watchlists gw ON w.id = gw.watchlist_id
            JOIN user_groups g ON gw.group_id = g.id
            WHERE g.groupName = ? AND (
                g.owner_id = (SELECT id FROM users WHERE username = ?) OR 
                EXISTS (SELECT 1 FROM group_members gm WHERE gm.group_id = g.id 
                        AND gm.user_id = (SELECT id FROM users WHERE username = ?))
            )""";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, groupName);
            ps.setString(2, username);
            ps.setString(3, username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lists.add(new PublicWatchlist(
                            rs.getInt("id"),
                            rs.getString("name")
                    ));
                }
            }
        }
        return lists;
    }


    // --- DISCOVER & SHARED ---

    public List<PublicWatchlist> getPublicWatchlists() throws SQLException {
        List<PublicWatchlist> lists = new ArrayList<>();
        String sql = "SELECT id, name FROM watchlists WHERE visibility = 'public'";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lists.add(new PublicWatchlist(rs.getInt("id"), rs.getString("name")));
            }
        }
        return lists;
    }

    public List<Item> getPublicListItemsById(int listId) {
        List<Item> items = new ArrayList<>();
        String sql = """
                SELECT title, content_type, genres, api_id, poster_url, priority, duration, release_year, added_date
                FROM list_items WHERE watchlist_id = ?""";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, listId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new Item(
                            rs.getString("title"),
                            rs.getString("content_type"),
                            rs.getString("genres"),
                            rs.getString("api_id"),
                            rs.getString("poster_url"),
                            rs.getInt("priority"),
                            rs.getInt("duration"),
                            rs.getInt("release_year"),
                            rs.getString("added_date")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    // --- HELPERS ---

    public int getWatchlistId(String username, String listName) throws SQLException {
        String sql = "SELECT w.id FROM watchlists w JOIN users u ON w.user_id = u.id WHERE u.username = ? AND w.name = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, listName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("id") : -1;
            }
        }
    }

    private int getGroupId(String username, String groupName) throws SQLException {
        String sql = """
                SELECT g.id FROM user_groups g 
                LEFT JOIN group_members gm ON g.id = gm.group_id
                WHERE g.groupName = ? AND (
                    g.owner_id = (SELECT id FROM users WHERE username = ?) OR 
                    gm.user_id = (SELECT id FROM users WHERE username = ?)
                ) LIMIT 1
                """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, groupName);
            ps.setString(2, username);
            ps.setString(3, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("id") : -1;
            }
        }
    }

    private String normalizeContentType(String apiType) {
        if (apiType == null) return "MOVIE";
        String type = apiType.toUpperCase();
        return (type.contains("TV") || type.contains("SERIES") || type.contains("SHOW")) ? "SERIES" : "MOVIE";
    }

    public boolean deleteItem(String username, String listName, String apiId) throws SQLException {
        int watchlistId = getWatchlistId(username, listName);
        if (watchlistId == -1) return false;

        String sql = "DELETE FROM list_items WHERE watchlist_id = ? AND api_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, watchlistId);
            ps.setString(2, apiId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteGroup(String username, String groupName) throws SQLException {
        String sql = """
                DELETE FROM user_groups WHERE groupName = ? 
                        AND owner_id = (SELECT id FROM users WHERE username = ?)""";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, groupName);
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        }
    }

    public String addWatchlistToGroup(String username, String groupName, String watchlistName) throws SQLException {
        int groupId = getGroupId(username, groupName);
        int watchlistId = getWatchlistId(username, watchlistName);

        if (groupId == -1 || watchlistId == -1) return "ERROR:NOT_FOUND";

        // SQL: Gruba watchlist ekleme
        String sql = "INSERT INTO group_watchlists (group_id, watchlist_id) VALUES (?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, watchlistId);
            ps.executeUpdate();
            return "SUCCESS";
        } catch (SQLException e) {
            // SQLite hata kodu 19: UNIQUE constraint failed (zaten ekli demektir)
            if (e.getErrorCode() == 19 || e.getMessage().contains("UNIQUE")) {
                return "ERROR:ALREADY_EXISTS";
            }
            throw e;
        }
    }
    public String getGroupCode(String username, String groupName) throws SQLException {
        String sql = "SELECT g.join_code FROM user_groups g " +
                "JOIN users u ON g.owner_id = u.id " +
                "WHERE u.username = ? AND g.groupName = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, groupName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("join_code") : null;
            }
        }
    }
}