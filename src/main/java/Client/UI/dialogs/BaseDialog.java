package Client.UI.dialogs;

import Client.UI.utils.UIConstants;

import javax.swing.*;
import java.awt.*;

public abstract class BaseDialog extends JDialog {

    protected JButton confirmButton;
    protected JButton cancelButton;

    public BaseDialog(JFrame parent, Dimension size, String title, String confirmButtonText) {
        super(parent, title, true);
        setSize(size);
        setResizable(false);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE); // Opsiyonel: Daha temiz bir görünüm için
        setContentPane(mainPanel);

        addContent(mainPanel);

        mainPanel.add(Box.createVerticalStrut(20));
        addButtons(mainPanel, confirmButtonText);

        getRootPane().setDefaultButton(confirmButton);
    }

    private void addButtons(Container container, String confirmText) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        cancelButton = new JButton("Cancel");
        confirmButton = new JButton(confirmText);

        styleButton(confirmButton, UIConstants.ADD_BUTTON_COLOR);
        styleButton(cancelButton, Color.GRAY);

        cancelButton.addActionListener(e -> dispose());
        confirmButton.addActionListener(e -> onConfirm());

        buttonPanel.add(cancelButton);
        buttonPanel.add(confirmButton);

        container.add(buttonPanel);
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setPreferredSize(new Dimension(130, 40));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));

        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    protected abstract void addContent(Container container);
    protected abstract void onConfirm();
}