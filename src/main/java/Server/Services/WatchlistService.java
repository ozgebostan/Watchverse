package Server.Services;

import Database.daos.WatchlistDAO;
import Model.Item;
import Model.PublicWatchlist;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WatchlistService {

    private final WatchlistDAO watchlistDao;

    public WatchlistService() {
        this.watchlistDao = WatchlistDAO.getInstance();
    }

    // Request for creating the watchlist
    public boolean createWatchlist(String username, String listName, String visibility) {
        if (isAnyBlank(listName)) return false;
        try {
            return watchlistDao.createWatchlist(username, listName, visibility);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Request for gathering the watch lists
    public List<String> getUserWatchlists(String username) {
        try {
            return watchlistDao.getUserWatchlists(username);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>(); // Return empty list instead of null for UI safety
        }
    }

    // Request for adding movie or series to the watchlist
    public String addItem(String username, String listName, Item item) {
        try {
            return watchlistDao.addItemToWatchlist(username, listName, item);
        } catch (SQLException e) {
            e.printStackTrace();
            return "ERROR:" + e.getMessage();
        }
    }

    // Request for gathering the contents of the watchlist
    public List<Item> getListItems(String username, String listName) {
        try {
            return watchlistDao.getItemsInWatchlist(username, listName);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public boolean deleteWatchlist(String username, String listName) {
        try {
            return watchlistDao.deleteWatchlist(username, listName);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<PublicWatchlist> getPublicWatchlists() {
        try {
            return watchlistDao.getPublicWatchlists();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Item> getPublicListItemsById(int listId) {
        List<Item> items = watchlistDao.getPublicListItemsById(listId);
        return (items != null) ? items : new ArrayList<>();
    }

    public boolean removeItem(String username, String listName, String apiId) {
        try {
            return watchlistDao.deleteItem(username, listName, apiId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createGroup(String username, String groupName) {
        if (isAnyBlank(groupName)) return false;
        try {
            return watchlistDao.createGroup(username, groupName);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getUserGroups(String username) {
        try {
            return watchlistDao.getUserGroups(username);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public String joinGroup(String username, String code) {
        if (isAnyBlank(code)) return "ERROR: EMPTY CODE";
        try {
            return watchlistDao.joinGroup(username, code.trim().toUpperCase());
        } catch (SQLException e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    public boolean addListToGroup(String username, String groupName, String listName) {
        try {
            return watchlistDao.addWatchlistToGroup(username, groupName, listName);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getGroupWatchlists(String username, String groupName) {
        try {
            return watchlistDao.getGroupWatchlists(username, groupName);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public String getGroupCode(String username, String groupName) {
        try {
            return watchlistDao.getGroupCode(username, groupName);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean deleteGroup(String username, String groupName) {
        try {
            return watchlistDao.deleteGroup(username, groupName);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- Helpers ---

    public String getWatchlistVisibility(String username, String listName) {
        try {
            return watchlistDao.getWatchlistVisibility(username, listName);
        } catch (SQLException e) {
            return "PRIVATE";
        }
    }

    private boolean isAnyBlank(String field) {
        return field == null || field.isBlank();
    }
}