package Client.UI.frames;

import Client.UI.panels.*;

public class MainFrame extends BaseFrame {

    public MainFrame() {
        super("Watchverse - Welcome");

        container.add(new Client.UI.panels.LoginPanel(this), "LOGIN");
        container.add(new RegisterPanel(this), "SIGNUP");
        container.add(new ForgotPasswordPanel(this), "FORGOT");
        container.add(new ResetPasswordPanel(this), "RESET");

        //Starting screen
        showScreen("LOGIN");

        setVisible(true);
    }

    public static void main(String[] args) {
        try {
            javax.swing.UIManager.setLookAndFeel(
                    javax.swing.UIManager.getSystemLookAndFeelClassName()
            );
        } catch (Exception ignored) {}

        javax.swing.SwingUtilities.invokeLater(MainFrame::new);
    }
}