package Client.UI.panels;

import Client.Services.AuthClient;
import Client.UI.frames.BaseFrame;
import Client.UI.utils.UIBehavior;
import Client.UI.utils.UIConstants;
import Client.UI.utils.UIMaker;
import Model.AuthResult;

import javax.swing.*;
import java.awt.*;

public class ResetPasswordPanel extends BaseAuthPanel {
    private JLabel passwordLabel;
    private JLabel confirmLabel;

    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JButton resetButton;

    private final String PASS_HINT = "Enter new password...";
    private final String CONFIRM_HINT = "Enter new password again...";

    private AuthClient authClient;
    private BaseFrame frame;

    public ResetPasswordPanel(BaseFrame frame) {
        super();
        this.frame = frame;
        this.authClient = new AuthClient();
    }

    @Override
    protected void build() {
        enableBackButton(() -> frame.showScreen("LOGIN"));

        setComponents();
        setComponentStyles();
        setEvents();

        add(passwordLabel);
        add(Box.createVerticalStrut(8));
        add(newPasswordField);

        add(Box.createVerticalStrut(15));

        add(confirmLabel);
        add(Box.createVerticalStrut(8));
        add(confirmPasswordField);

        add(Box.createVerticalStrut(25));
        add(resetButton);

        add(Box.createVerticalStrut(10));
        setErrorLabel();
    }

    private void onReset() {
        hideError();

        String targetUser = ForgotPasswordPanel.verifiedUsername;

        if (targetUser == null) {
            showError("Session timed out. Start over.");
            Timer timer = new Timer(2000, e -> frame.showScreen("LOGIN"));
            timer.setRepeats(false);
            timer.start();
            return;
        }

        String newPass = new String(newPasswordField.getPassword());
        String confirmPass = new String(confirmPasswordField.getPassword());

        if (newPass.equals(PASS_HINT) || confirmPass.equals(CONFIRM_HINT)) {
            showError("Please fill in both password fields.");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            showError("Passwords do not match.");
            return;
        }

        if (!authClient.isPasswordStrong(newPass)) {
            JOptionPane.showMessageDialog(frame,
                    "Password must be 8+ chars, 1 Upper, 1 Number.",
                    "Weak Password",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        AuthResult result = authClient.forgotPassword(targetUser, newPass);

        if (result == AuthResult.SUCCESS || result == AuthResult.PASSWORD_UPDATED) {
            JOptionPane.showMessageDialog(frame, "Success! Your password has been updated.");
            ForgotPasswordPanel.verifiedUsername = null;
            frame.showScreen("LOGIN");
        } else {
            showError("Update failed: " + result);
        }
    }

    @Override
    protected void resetFields() {
        UIMaker.resetPasswordField(newPasswordField, PASS_HINT);
        UIMaker.resetPasswordField(confirmPasswordField, CONFIRM_HINT);
        hideError();
    }

    private void setComponents() {
        passwordLabel = new JLabel("New Password");
        confirmLabel = new JLabel("Confirm Password");
        newPasswordField = new JPasswordField(PASS_HINT);
        confirmPasswordField = new JPasswordField(CONFIRM_HINT);
        resetButton = new JButton("Reset Password");
    }

    private void setComponentStyles() {
        passwordLabel.setAlignmentX(CENTER_ALIGNMENT);
        confirmLabel.setAlignmentX(CENTER_ALIGNMENT);

        UIMaker.styleLabel(passwordLabel, UIConstants.LABEL_COLOR);
        UIMaker.styleLabel(confirmLabel, UIConstants.LABEL_COLOR);
        UIMaker.stylePasswordField(newPasswordField, true);
        UIMaker.stylePasswordField(confirmPasswordField, true);
        UIMaker.styleButton(resetButton, UIConstants.COMP_SIZE);
        resetButton.setAlignmentX(CENTER_ALIGNMENT);
    }

    private void setEvents() {
        UIBehavior.setPasswordPlaceholder(newPasswordField, PASS_HINT);
        UIBehavior.setPasswordPlaceholder(confirmPasswordField, CONFIRM_HINT);

        resetButton.addActionListener(e -> onReset());
        confirmPasswordField.addActionListener(e -> onReset());
    }
}