package Client.UI.panels;

import Client.UI.frames.BaseFrame;
import Client.Services.AuthClient;
import Model.AuthResult;
import Client.UI.utils.UIBehavior;
import Client.UI.utils.UIConstants;
import Client.UI.utils.UIMaker;

import javax.swing.*;
import java.awt.*;

public class ForgotPasswordPanel extends BaseAuthPanel {
    private JLabel usernameLabel;
    private JTextField usernameField;
    private JButton verifyButton;

    private AuthClient authClient;
    private final BaseFrame frame;

    private final String USER_HINT = "Enter username...";

    public static String verifiedUsername = null;

    public ForgotPasswordPanel(BaseFrame frame) {
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

        add(usernameLabel);
        add(Box.createVerticalStrut(10));
        add(usernameField);
        add(Box.createVerticalStrut(25));
        add(verifyButton);

        add(Box.createVerticalStrut(10));
        setErrorLabel();
    }

    private void onVerify() {
        hideError();
        String inputUser = usernameField.getText().trim();

        if (inputUser.isEmpty() || inputUser.equals(USER_HINT)) {
            showError("Please enter your username.");
            return;
        }

        if (!authClient.isUserExists(inputUser)) {
            showError("User not found.");
            return;
        }

        String question = authClient.getSecurityQuestion(inputUser);

        String answer = JOptionPane.showInputDialog(frame,
                "Security Question: " + question,
                "Identity Verification",
                JOptionPane.QUESTION_MESSAGE);

        if (answer == null || answer.trim().isEmpty()) {
            return;
        }

        AuthResult result = authClient.verifySecurityAnswer(inputUser, answer.trim());

        if (result == AuthResult.SUCCESS) {
            verifiedUsername = inputUser;
            JOptionPane.showMessageDialog(frame, "Identity Verified! Set your new password.");
            frame.showScreen("RESET");
        } else {
            showError("The answer is incorrect.");
        }
    }

    @Override
    protected void resetFields() {
        UIMaker.resetField(usernameField, USER_HINT);
        verifiedUsername = null;
        hideError();
    }

    private void setComponents() {
        usernameLabel = new JLabel("Username");
        usernameField = new JTextField(USER_HINT);
        verifyButton = new JButton("Verify Identity");
    }

    private void setComponentStyles() {
        usernameLabel.setAlignmentX(CENTER_ALIGNMENT);
        UIMaker.styleLabel(usernameLabel, UIConstants.LABEL_COLOR);
        UIMaker.styleField(usernameField, true);
        UIMaker.styleButton(verifyButton, UIConstants.COMP_SIZE, UIConstants.ADD_BUTTON_COLOR);
        verifyButton.setAlignmentX(CENTER_ALIGNMENT);
    }

    private void setEvents() {
        UIBehavior.setTextFieldPlaceholder(usernameField, USER_HINT);
        verifyButton.addActionListener(e -> onVerify());
        usernameField.addActionListener(e -> verifyButton.doClick());
    }
}