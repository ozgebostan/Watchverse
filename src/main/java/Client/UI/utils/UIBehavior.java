package Client.UI.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 *General gui behaviors of the app
 */
public final class UIBehavior {

    private UIBehavior() {}

    public static void setTextFieldPlaceholder(JTextField textField, String hintText) {
        textField.setText(hintText);
        textField.setForeground(UIConstants.HINT_GRAY);

        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(hintText)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().trim().isEmpty()) {
                    textField.setText(hintText);
                    textField.setForeground(UIConstants.HINT_GRAY);
                }
            }
        });
    }

    public static void setPasswordPlaceholder(JPasswordField passwordField, String hintText) {
        passwordField.setText(hintText);
        passwordField.setEchoChar((char) 0);
        passwordField.setForeground(UIConstants.HINT_GRAY);

        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                String current = new String(passwordField.getPassword());
                if (current.equals(hintText)) {
                    passwordField.setText("");
                    passwordField.setEchoChar('•');
                    passwordField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (passwordField.getPassword().length == 0) {
                    passwordField.setText(hintText);
                    passwordField.setEchoChar((char) 0);
                    passwordField.setForeground(UIConstants.HINT_GRAY);
                }
            }
        });
    }
}
