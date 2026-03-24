package Client.Services;

import Client.Network.SocketManager;
import Model.AuthResult;

/**
 * CLIENT-SIDE AuthService
 *
 *
 */
public class AuthClient {

    // 1. Login
    public AuthResult login(String username, String password) {
        Object response = SocketManager.getInstance().sendRequest("LOGIN###" + username + "###" + password);
        return parseResponse(response);
    }

    // 2. Register
    public AuthResult register(String user, String pass, String ques, String ans) {
        Object response = SocketManager.getInstance().sendRequest("REGISTER###" + user + "###" + pass + "###" + ques + "###" + ans);
        return parseResponse(response);
    }

    // 3. User control for forgot password panel
    public boolean isUserExists(String username) {
        Object response = SocketManager.getInstance().sendRequest("CHECK_USER###" + username);
        return "EXISTS".equals(response);
    }

    // 4. Security Questing for forgot password panel
    public String getSecurityQuestion(String username) {
        Object response = SocketManager.getInstance().sendRequest("GET_QUESTION###" + username);
        return (response instanceof String) ? (String) response : "Security question not found.";
    }

    // 5. Validate security answer
    public AuthResult verifySecurityAnswer(String username, String answer) {
        Object response = SocketManager.getInstance().sendRequest("VERIFY_ANSWER###" + username + "###" + answer);
        return parseResponse(response);
    }

    // 6. Reset password
    public AuthResult forgotPassword(String username, String newPassword) {
        Object response = SocketManager.getInstance().sendRequest("RESET_PASSWORD###" + username + "###" + newPassword);
        return parseResponse(response);
    }

    public boolean isPasswordStrong(String password) {
        if (password == null) return false;
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{6,}$");
    }

    private AuthResult parseResponse(Object response) {
        if (response == null) return AuthResult.ERROR;

        String respStr = response.toString();
        String status = respStr.split("###")[0];

        try {
            return AuthResult.valueOf(status);
        } catch (Exception e) {
            System.err.println("Enum error: " + status);
            return AuthResult.ERROR;
        }
    }
}