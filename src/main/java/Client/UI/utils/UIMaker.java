package Client.UI.utils;

import javax.swing.*;

import java.awt.*;

import static Client.UI.utils.UIConstants.*;

public final class UIMaker {


    private UIMaker() {
    }

    public static void styleLabel(JLabel label, Color color) {
        label.setForeground(color);
        label.setFont(LABEL_FONT);

        //label content left aligned
        label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        label.setHorizontalAlignment(SwingConstants.LEFT);

        //without scaling the label, it doesn't align to left for some reason
        label.setMaximumSize(new Dimension(COMP_SIZE.width, label.getPreferredSize().height));
    }


    public static void styleErrorLabel(JLabel errorLabel) {
        errorLabel.setForeground(Color.RED);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
    }
    public static void styleLinkLabel(JLabel label) {
        label.setForeground(LINK_COLOR);
        label.setFont(LINK_FONT);
    }

    public static void styleField(JTextField field, boolean hasHintText) {
        field.setMaximumSize(COMP_SIZE);

        field.setFont(FIELD_FONT);

        if (hasHintText) {
            field.setForeground(HINT_GRAY);
        }
    }

    public static void styleComboBox(JComboBox<?> box) {
        box.setMaximumSize(new Dimension(330, 35));
        box.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.setFont(new Font("Segoe UI", Font.BOLD, 16));
        box.setFocusable(false);
    }

    public static void stylePasswordField(JPasswordField field, boolean hasHintText) {
        field.setMaximumSize(COMP_SIZE);
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        field.setFont(PASSWORDFIELD_FONT);

        //start state
        if (hasHintText) {
            field.setEchoChar((char) 0);
            field.setForeground(HINT_GRAY);
        }
    }


    public static void styleButton(JButton button, Dimension dimensions, Color color) {
        button.setMaximumSize(new Dimension(dimensions.width, dimensions.height));
        button.setPreferredSize(new Dimension(dimensions.width, dimensions.height));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        button.setFont(LABEL_FONT);

        button.setBackground(color);
        button.setForeground(Color.WHITE);

        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void styleBackButton(JButton button) {
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorderPainted(false);

        button.setForeground(LINK_COLOR);
        button.setFont(new Font("Segoe UI", Font.BOLD, 38));

    }
    public static void resetField(JTextField field, String hint) {
        field.setText(hint);
        field.setForeground(HINT_GRAY);
    }

    public static void resetPasswordField(JPasswordField field, String hint) {
        field.setText(hint);
        field.setForeground(HINT_GRAY);
        field.setEchoChar((char) 0);
    }
}
