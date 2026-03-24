package Server.Services;

import Database.daos.UserDAO;
import Model.AuthResult;
import java.sql.SQLException;

public class AuthService {
    private final UserDAO userDao;

    public AuthService() {
        this.userDao = UserDAO.getInstance();
    }

    public AuthResult login(String username, String password) {
        try {
            if (username.isBlank() || password.isBlank()) return AuthResult.EMPTY_FIELDS;
            if (userDao.validateLogin(username, password)) return AuthResult.SUCCESS;
            return AuthResult.WRONG_PASSWORD;
        } catch (SQLException e) {
            return AuthResult.ERROR;
        }
    }

    public AuthResult register(String username, String password, String question, String answer) {
        try {
            if (username.isBlank() || password.isBlank()) return AuthResult.EMPTY_FIELDS;
            if (password.length() < 6) return AuthResult.WEAK_PASSWORD;

            boolean success = userDao.registerUser(username, password, question, answer);
            return success ? AuthResult.SUCCESS : AuthResult.USER_ALREADY_EXISTS;
        } catch (SQLException e) {
            return AuthResult.ERROR;
        }
    }

    public boolean isUserExists(String username) throws SQLException {
        return userDao.isUserExists(username);
    }

    public String getSecurityQuestion(String username) throws SQLException {
        return userDao.getSecurityQuestion(username);
    }

    public AuthResult verifySecurityAnswer(String username, String answer) {
        try {
            String realAnswer = userDao.getSecurityAnswer(username);
            return (realAnswer != null && realAnswer.equalsIgnoreCase(answer))
                    ? AuthResult.SUCCESS : AuthResult.WRONG_SECURITY_ANSWER;
        } catch (SQLException e) {
            return AuthResult.ERROR;
        }
    }

    public AuthResult forgotPassword(String username, String newPassword) {
        try {
            return userDao.updatePassword(username, newPassword)
                    ? AuthResult.PASSWORD_UPDATED : AuthResult.ERROR;
        } catch (SQLException e) {
            return AuthResult.ERROR;
        }
    }

    public AuthResult deleteAccount(String username, String password) {
        try {
            if (userDao.validateLogin(username, password)) {
                return userDao.deleteUser(username) ? AuthResult.SUCCESS : AuthResult.ERROR;
            }
            return AuthResult.WRONG_PASSWORD;
        } catch (SQLException e) {
            return AuthResult.ERROR;
        }
    }
}