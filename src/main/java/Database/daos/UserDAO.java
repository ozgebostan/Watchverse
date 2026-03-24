package Database.daos;

import Database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO class responsible for handling
 * user-related database operations.
 * (login & register) and many more
 */

public class UserDAO {
    private static UserDAO instance;
    private final DatabaseManager dbManager;

    private UserDAO() {
        this.dbManager = DatabaseManager.getInstance();

    }

    public static synchronized UserDAO getInstance() {
        if (instance == null) {
            instance = new UserDAO();
        }
        return instance;
    }

    //Using prepared statement against SQL injections, more safe
    public boolean isUserExists(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ? LIMIT 1";
        try (Connection connection = dbManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public boolean registerUser(String username, String password, String question, String answer) throws SQLException {
        if (isUserExists(username)) return false;

        String sql = "INSERT INTO users (username, password, security_question, security_answer) VALUES (?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, question);
            pstmt.setString(4, answer);

            return pstmt.executeUpdate() == 1;
        }
    }


    public String getSecurityQuestion(String username) throws SQLException {
        String sql = "SELECT security_question FROM users WHERE username = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("security_question");
                }
            }
        }
        return null;
    }

    public boolean validateLogin(String username, String password) throws SQLException {
        String sql = "SELECT password FROM users WHERE username = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    return storedPassword.equals(password);
                }
            }
        }
        return false;
    }

    public boolean updatePassword(String username, String newPassword) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE username = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newPassword);
            pstmt.setString(2, username);

            return pstmt.executeUpdate() == 1;
        }
    }

    /**
     * Retrieves the security answer for a given username.
     * Used for verifying password recovery identity.
     */
    public String getSecurityAnswer(String username) throws SQLException {
        String sql = "SELECT security_answer FROM users WHERE username = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("security_answer");
                }
            }
        }
        return null;
    }

    /**
     * Permanently deletes a user account from the database.
     * Due to CASCADE, this will also delete their watchlists and items.
     */
    public boolean deleteUser(String username) throws SQLException {
        String sql = "DELETE FROM users WHERE username = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            return pstmt.executeUpdate() == 1;
        }
    }
}
