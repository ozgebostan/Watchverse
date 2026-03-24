package Client.UI.frames;

import Client.UI.panels.BackgroundImage;

import javax.swing.*;
import java.awt.*;

public abstract class BaseFrame extends JFrame {
    protected CardLayout cardLayout;
    protected JPanel container;

    protected BaseFrame(String title) {
        setTitle(title);
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        BackgroundImage bg = new BackgroundImage();
        bg.setLayout(new BorderLayout());
        this.setContentPane(bg);

        // 2. CardLayout Container
        cardLayout = new CardLayout();
        container = new JPanel(cardLayout);
        container.setOpaque(false);


        getContentPane().add(container, BorderLayout.CENTER);
    }

    /**
     * Switching screens
     */
    public void showScreen(String name) {
        cardLayout.show(container, name);

        container.revalidate();
        container.repaint();
    }
}