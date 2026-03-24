package Client.panels;

import Client.UI.panels.BaseAuthPanel;
import Client.UI.frames.BaseFrame;
import Client.Services.AuthClient;
import Model.AuthResult;
import Client.UI.utils.UIConstants;
import Client.UI.utils.UIMaker;
import Model.ClientUserSession;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginPanel extends BaseAuthPanel {
    private JLabel usernameLabel;
    private JTextField usernameField;
    private JLabel passwordLabel;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel signupLabel;
    private JLabel forgotLabel;

    private AuthClient authClient;
    private BaseFrame frame;

    public LoginPanel(BaseFrame frame) {
        this.frame = frame;
        this.authClient = new AuthClient();
    }

    @Override
    protected void build() {
        hideBackButton();
        setComponents();
        setComponentStyles();
        setEvents();


        add(usernameLabel);
        add(Box.createVerticalStrut(5));
        add(usernameField);
        add(Box.createVerticalStrut(15));

        JPanel passwordRow = new JPanel(new BorderLayout());
        passwordRow.setMaximumSize(new Dimension(UIConstants.COMP_SIZE.width, 25));
        passwordRow.setOpaque(false);
        passwordRow.add(passwordLabel, BorderLayout.WEST);
        passwordRow.add(forgotLabel, BorderLayout.EAST);

        add(passwordRow);
        add(Box.createVerticalStrut(5));
        add(passwordField);
        add(Box.createVerticalStrut(25));

        // Login Button
        add(loginButton);
        add(Box.createVerticalStrut(20));

        setErrorLabel();
        add(Box.createVerticalStrut(10));

        JPanel signupRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        signupRow.setOpaque(false);
        JLabel haveAccount = new JLabel("Don't have an account? ");
        haveAccount.setForeground(UIConstants.LABEL_COLOR);
        haveAccount.setFont(UIConstants.LINK_FONT);

        signupRow.add(haveAccount);
        signupRow.add(signupLabel);
        signupRow.setMaximumSize(new Dimension(UIConstants.COMP_SIZE.width, 30));

        add(signupRow);
    }

    private void onLogin() {
        hideError();
        String uName = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());

        if (uName.isEmpty() || pass.isEmpty()) {
            showError("Please fill all fields.");
            return;
        }

        AuthResult result = authClient.login(uName, pass);

        if (result == AuthResult.SUCCESS) {
            ClientUserSession.getInstance().setUsername(uName);
            JOptionPane.showMessageDialog(frame, "Login Successful! Welcome " + uName);

            frame.dispose();
            SwingUtilities.invokeLater(AppFrame::new);
        } else {
            handleLoginError(result);
        }
    }

    private void handleLoginError(AuthResult result) {
        switch (result) {
            case USER_NOT_FOUND -> showError("User not found.");
            case WRONG_PASSWORD -> showError("Incorrect password.");
            case ERROR -> showError("Connection failed. Check your server.");
            default -> showError("Login failed: " + result);
        }
    }

    @Override
    protected void resetFields() {
        usernameField.setText("");
        passwordField.setText("");
        hideError();
    }

    private void setComponents() {
        usernameLabel = new JLabel("Username");
        usernameField = new JTextField();
        passwordLabel = new JLabel("Password");
        passwordField = new JPasswordField();
        loginButton = new JButton("Log In");
        forgotLabel = new JLabel("Forgot password?");
        signupLabel = new JLabel("Sign Up");
    }

    private void setComponentStyles() {
        usernameLabel.setAlignmentX(CENTER_ALIGNMENT);
        passwordLabel.setAlignmentX(CENTER_ALIGNMENT);

        UIMaker.styleLabel(usernameLabel, UIConstants.LABEL_COLOR);
        UIMaker.styleLabel(passwordLabel, UIConstants.LABEL_COLOR);
        UIMaker.styleField(usernameField, false);
        UIMaker.stylePasswordField(passwordField, false);
        UIMaker.styleLinkLabel(forgotLabel);
        UIMaker.styleLinkLabel(signupLabel);
        UIMaker.styleButton(loginButton);

        forgotLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signupLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void setEvents() {
        forgotLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { frame.showScreen("FORGOT"); }
        });

        signupLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { frame.showScreen("SIGNUP"); }
        });

        loginButton.addActionListener(e -> onLogin());
        passwordField.addActionListener(e -> onLogin());
    }
}