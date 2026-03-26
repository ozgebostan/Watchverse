package Client.UI.panels;

import Client.UI.utils.LogoMaker;
import Client.UI.utils.UIMaker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

//Template Method Pattern
public abstract class BaseAuthPanel extends JPanel {
    private JButton backButton = new JButton("←");
    private JLabel errorLabel;

    protected BaseAuthPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        setOpaque(false);

        // 1. Back Button
        UIMaker.styleBackButton(backButton);
        backButton.setAlignmentX(LEFT_ALIGNMENT);

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
        top.setOpaque(false);
        top.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        top.add(backButton);
        top.add(Box.createHorizontalGlue());
        add(top);

        LogoMaker.addLogoTo(this, "/Logo.png", 200, 200, CENTER_ALIGNMENT, 20);

        build();


        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                if (errorLabel != null) {
                    hideError();
                }
                resetFields();
            }
        });
    }

    protected abstract void build();
    protected abstract void resetFields();

    protected void enableBackButton(Runnable action) {
        backButton.setVisible(true);
        for (ActionListener al : backButton.getActionListeners()) {
            backButton.removeActionListener(al);
        }
        backButton.addActionListener(e -> action.run());
    }

    protected void hideBackButton() {
        backButton.setVisible(false);
    }

    protected void setErrorLabel() {
        errorLabel = new JLabel(" ");
        UIMaker.styleErrorLabel(errorLabel);
        errorLabel.setVisible(false);
        add(errorLabel);
    }

    protected void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            revalidate();
        }
    }

    protected void hideError() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }
    }
}