package Client.UI.frames;

import Client.UI.dialogs.SettingsDialog;

import Client.UI.panels.WatchversePanel;
import Model.ClientUserSession;

import javax.swing.*;
import java.awt.*;

/**
 * Main app panel after login
 */
public class WatchverseFrame extends BaseFrame {

    private WatchversePanel watchversePanel;

    public WatchverseFrame() {
        super("Watchverse - " + ClientUserSession.getInstance().getUsername());

        setExtendedState(JFrame.MAXIMIZED_BOTH);

        initializePanel();

        setVisible(true);
    }

    private void initializePanel() {
        watchversePanel = new WatchversePanel(this);

        container.add(watchversePanel, "MAIN_APP");
        showScreen("MAIN_APP");
    }

    public void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out?",
                "Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            ClientUserSession.getInstance().logout();

            this.dispose();

            SwingUtilities.invokeLater(MainFrame::new);
        }
    }

    public void openSettings() {
        new SettingsDialog(this).setVisible(true);
    }
}