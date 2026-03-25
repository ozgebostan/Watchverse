package Client.UI.frames;

import Client.UI.dialogs.SettingsDialog;

import Client.UI.panels.AppPanel;
import Model.ClientUserSession;

import javax.swing.*;
import java.awt.*;

/**
 * Giriş yaptıktan sonra açılan ana uygulama penceresi.
 */
public class WatchverseFrame extends BaseFrame {

    private AppPanel appPanel;

    public WatchverseFrame() {
        super("Watchverse - " + ClientUserSession.getInstance().getUsername());

        setExtendedState(JFrame.MAXIMIZED_BOTH);

        initializePanel();

        setVisible(true);
    }

    private void initializePanel() {
        appPanel = new AppPanel(this);

        container.add(appPanel, "MAIN_APP");
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