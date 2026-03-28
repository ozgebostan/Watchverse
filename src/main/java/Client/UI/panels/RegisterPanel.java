package Client.UI.panels;

import Client.Services.AuthClient;
import Client.UI.frames.BaseFrame;
import Client.UI.utils.SecurityConstants;
import Client.UI.utils.UIBehavior;
import Client.UI.utils.UIConstants;
import Client.UI.utils.UIMaker;
import Model.AuthResult;

import javax.swing.*;
import java.awt.*;

import static Client.UI.utils.UIConstants.*;

public class RegisterPanel extends BaseAuthPanel {
    private final BaseFrame frame;
    private final String USER_HINT = "Enter username...";
    private final String PASS_HINT = "Enter password...";
    private final String CONFIRM_HINT = "Enter password again...";
    private final String ANSWER_HINT = "Enter your answer...";
    private JPanel step1Panel, step2Panel, cardPanel;
    private CardLayout cardLayout;
    private JLabel username, password;
    private JTextField usernameField, securityAnswerField;
    private JPasswordField passwordField, confirmPasswordField;
    private JComboBox<String> securityQuestionBox;
    private JButton nextButton, signupButton;
    private String tempUsername, tempPassword;
    private final AuthClient authClient;

    public RegisterPanel(BaseFrame frame) {
        super();
        this.frame = frame;
        this.authClient = new AuthClient();
    }

    @Override
    protected void build() {
        setComponents();
        setComponentStyles();
        setEvents();

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);

        prepareStep1Panel();
        prepareStep2Panel();

        cardPanel.add(step1Panel, "STEP1");
        cardPanel.add(step2Panel, "STEP2");

        add(cardPanel);
        SwingUtilities.invokeLater(this::showStep1);
    }

    private void prepareStep1Panel() {
        step1Panel = new JPanel();
        step1Panel.setLayout(new BoxLayout(step1Panel, BoxLayout.Y_AXIS));
        step1Panel.setOpaque(false);

        step1Panel.add(username);
        step1Panel.add(Box.createVerticalStrut(10));
        step1Panel.add(usernameField);
        step1Panel.add(Box.createVerticalStrut(20));
        step1Panel.add(password);
        step1Panel.add(Box.createVerticalStrut(10));
        step1Panel.add(passwordField);
        step1Panel.add(Box.createVerticalStrut(15));
        step1Panel.add(confirmPasswordField);
        step1Panel.add(Box.createVerticalStrut(20));
        step1Panel.add(nextButton);
    }

    private void prepareStep2Panel() {
        step2Panel = new JPanel();
        step2Panel.setLayout(new BoxLayout(step2Panel, BoxLayout.Y_AXIS));
        step2Panel.setOpaque(false);

        step2Panel.add(Box.createVerticalStrut(20));
        step2Panel.add(securityQuestionBox);
        step2Panel.add(Box.createVerticalStrut(30));
        step2Panel.add(securityAnswerField);
        step2Panel.add(Box.createVerticalStrut(20));
        step2Panel.add(signupButton);
    }

    private void showStep1() {
        cardLayout.show(cardPanel, "STEP1");
        enableBackButton(() -> frame.showScreen("LOGIN"));
    }

    private void showStep2() {
        cardLayout.show(cardPanel, "STEP2");
        enableBackButton(this::showStep1);
    }

    @Override
    protected void resetFields() {
        UIMaker.resetField(usernameField, USER_HINT);
        UIMaker.resetPasswordField(passwordField, PASS_HINT);
        UIMaker.resetPasswordField(confirmPasswordField, CONFIRM_HINT);

        if (securityQuestionBox.getItemCount() > 0) securityQuestionBox.setSelectedIndex(0);
        UIMaker.resetField(securityAnswerField, ANSWER_HINT);

        tempUsername = null;
        tempPassword = null;
        showStep1();
    }

    private void setComponents() {
        username = new JLabel("Username");
        usernameField = new JTextField(USER_HINT);

        password = new JLabel("Password");
        passwordField = new JPasswordField(PASS_HINT);
        confirmPasswordField = new JPasswordField(CONFIRM_HINT);

        nextButton = new JButton("Next Step →");

        securityQuestionBox = new JComboBox<>(SecurityConstants.SECURITY_QUESTIONS);
        securityAnswerField = new JTextField(ANSWER_HINT);
        signupButton = new JButton("Sign Up");

        setErrorLabel();
    }

    private void setComponentStyles() {
        UIMaker.styleLabel(username, UIConstants.LABEL_COLOR);
        UIMaker.styleLabel(password, UIConstants.LABEL_COLOR);
        UIMaker.styleField(usernameField, true);
        UIMaker.stylePasswordField(passwordField, true);
        UIMaker.stylePasswordField(confirmPasswordField, true);
        UIMaker.styleButton(nextButton, COMP_SIZE);
        nextButton.setAlignmentX(CENTER_ALIGNMENT);

        UIMaker.styleComboBox(securityQuestionBox);

        //security answer field
        UIMaker.styleField(securityAnswerField, true);
        securityAnswerField.setMaximumSize(new Dimension(330, COMP_SIZE.height));


        //signup button
        UIMaker.styleButton(signupButton, new Dimension(330, COMP_SIZE.height));

    }

    private void setEvents() {
        UIBehavior.setTextFieldPlaceholder(usernameField, USER_HINT);
        UIBehavior.setPasswordPlaceholder(passwordField, PASS_HINT);
        UIBehavior.setPasswordPlaceholder(confirmPasswordField, CONFIRM_HINT);
        UIBehavior.setTextFieldPlaceholder(securityAnswerField, ANSWER_HINT);

        nextButton.addActionListener(e -> onNextStep());
        signupButton.addActionListener(e -> onSignup());
    }

    private void onNextStep() {
        hideError();
        String uName = usernameField.getText();
        String pass = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());

        if (uName.equals(USER_HINT) || pass.equals(PASS_HINT)) {
            showError("Please fill in all fields.");
            return;
        }
        if (!pass.equals(confirm)) {
            showError("Passwords do not match.");
            return;
        }
        if (authClient.isPasswordStrong(pass)) {
            JOptionPane.showMessageDialog(frame, "Password must be 6+ chars, 1 Upper, 1 Number.");
            return;
        }
        if (authClient.isUserExists(uName)) {
            showError("Username is already taken.");
            return;
        }

        this.tempUsername = uName;
        this.tempPassword = pass;
        showStep2();
    }

    private void onSignup() {
        hideError();
        String question = (String) securityQuestionBox.getSelectedItem();
        String answer = securityAnswerField.getText();

        if (securityQuestionBox.getSelectedIndex() == 0) {
            showError("Please select a security question.");
            return;
        }
        if (answer.equals(ANSWER_HINT)) {
            showError("Please enter an answer.");
            return;
        }

        AuthResult result = authClient.register(tempUsername, tempPassword, question, answer);

        if (result == AuthResult.SUCCESS) {
            JOptionPane.showMessageDialog(frame, "Account successfully created!");
            resetFields();
            frame.showScreen("LOGIN");
        } else {
            showError("Registration failed: " + result);
        }
    }
}