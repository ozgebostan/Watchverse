package Client.UI.dialogs;

import Client.Network.SocketManager;
import Client.UI.frames.MainFrame;
import Client.UI.utils.UIConstants;
import Client.UI.utils.UIMaker;
import Model.ClientUserSession;

import javax.swing.*;
import java.awt.*;

public class SettingsDialog extends BaseDialog {

    private final JFrame frame;
    private final String currentUsername;

    private JPasswordField oldPasswordField;
    private JPasswordField newPasswordField;
    private JButton deleteAccountButton;

    public SettingsDialog(JFrame parent) {
        super(parent, new Dimension(400, 450), "Settings", "Save Changes");
        this.currentUsername = ClientUserSession.getInstance().getUsername();
        this.frame = parent;
        buildUI();
    }

    @Override
    protected void addContent(Container container) {
        JLabel oldPasswordLabel = new JLabel("Old Password");
        JLabel newPasswordLabel = new JLabel("New Password");

        oldPasswordField = new JPasswordField();
        newPasswordField = new JPasswordField();
        deleteAccountButton = new JButton("Delete Account");

        container.setBackground(UIConstants.MAIN_APP_COLOR);
        UIMaker.styleLabel(oldPasswordLabel, Color.BLACK);
        UIMaker.styleLabel(newPasswordLabel, Color.BLACK);
        UIMaker.styleField(newPasswordField, false);
        UIMaker.styleField(oldPasswordField, false);

        container.add(oldPasswordLabel);
        container.add(Box.createVerticalStrut(5));
        container.add(oldPasswordField);
        container.add(Box.createVerticalStrut(10));
        container.add(newPasswordLabel);
        container.add(Box.createVerticalStrut(5));
        container.add(newPasswordField);
        container.add(Box.createVerticalStrut(20));

        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        container.add(separator);
        container.add(Box.createVerticalStrut(20));

        JLabel dangerLabel = new JLabel("Danger Zone");
        dangerLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        UIMaker.styleLabel(dangerLabel, UIConstants.DELETE);

        UIMaker.styleButton(deleteAccountButton, UIConstants.COMP_SIZE, UIConstants.DELETE);

        deleteAccountButton.addActionListener(e -> onDeleteAccount());

        container.add(dangerLabel);
        container.add(Box.createVerticalStrut(10));
        container.add(deleteAccountButton);
        container.add(Box.createVerticalStrut(20));

    }

    @Override
    protected void onConfirm() {
        String oldPass = new String(oldPasswordField.getPassword());
        String newPass = new String(newPasswordField.getPassword());

        if (oldPass.isBlank() || newPass.isBlank()) {
            JOptionPane.showMessageDialog(this, "Fields cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Object res = SocketManager.getInstance().sendRequest("CHANGE_PASSWORD###" + currentUsername + "###" + oldPass + "###" + newPass);

        if (res == null) {
            JOptionPane.showMessageDialog(this, "Server error.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String resultStr = res.toString();
        if (resultStr.equals("PASSWORD_UPDATED") || resultStr.equals("SUCCESS")) {
            JOptionPane.showMessageDialog(this, "Password updated successfully!");
            dispose();
        } else if (resultStr.equals("WRONG_PASSWORD")) {
            JOptionPane.showMessageDialog(this, "Old password is incorrect!", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Update failed: " + resultStr, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDeleteAccount() {
        String password = JOptionPane.showInputDialog(this,
                "Enter your password to confirm deletion:",
                "Confirm Account Deletion",
                JOptionPane.WARNING_MESSAGE);

        if (password == null || password.isBlank()) return;

        int choice = JOptionPane.showConfirmDialog(this,
                "This will permanently delete your account and all data.\nAre you absolutely sure?",
                "FINAL WARNING",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            Object res = SocketManager.getInstance().sendRequest("DELETE_ACCOUNT###" + currentUsername + "###" + password);

            if (res != null && (res.toString().equals("SUCCESS") || res.toString().equals("ACCOUNT_DELETED"))) {
                JOptionPane.showMessageDialog(this, "Account deleted. Goodbye!");
                dispose();
                if (frame != null) frame.dispose();
                ClientUserSession.getInstance().logout();
                new MainFrame();
            } else {
                String errorMsg = (res != null && res.toString().equals("WRONG_PASSWORD")) ? "Incorrect password!" : "Error deleting account.";
                JOptionPane.showMessageDialog(this, errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}