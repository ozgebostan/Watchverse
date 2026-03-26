package Client.UI.dialogs;

import Client.Network.SocketManager;
import Client.UI.utils.UIConstants;
import Client.UI.utils.UIMaker;
import Model.ClientUserSession;

import javax.swing.*;
import java.awt.*;

import static Client.UI.utils.UIConstants.FIELD_FONT;

public class AddWatchlist extends BaseDialog {

    private JTextField watchlistNameField;
    private JComboBox<String> typeBox;

    public AddWatchlist(JFrame frame) {
        super(frame, new Dimension(400, 350), "Create Watchlist", "Create");
    }

    @Override
    protected void addContent(Container container) {
        JLabel nameLabel = new JLabel("Watchlist Name:");
        watchlistNameField = new JTextField();

        JLabel typeLabel = new JLabel("Visibility Type:");
        typeBox = new JComboBox<>(new String[]{"Private", "Public", "Link-Only"});
        typeBox.setFont(UIConstants.LABEL_FONT);


        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        watchlistNameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        typeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        typeBox.setAlignmentX(Component.LEFT_ALIGNMENT);


        UIMaker.styleLabel(nameLabel, Color.BLACK);

        watchlistNameField.setFont(FIELD_FONT);
        watchlistNameField.setMaximumSize(new Dimension(152, 35));
        UIMaker.styleLabel(typeLabel, Color.BLACK);

        typeBox.setMaximumSize(new Dimension(152, 35));
        typeBox.setBackground(Color.WHITE);

        container.setBackground(UIConstants.MAIN_APP_COLOR);
        container.add(nameLabel);
        container.add(Box.createVerticalStrut(6));
        container.add(watchlistNameField);

        container.add(Box.createVerticalStrut(20));

        container.add(typeLabel);
        container.add(Box.createVerticalStrut(6));
        container.add(typeBox);

        container.add(Box.createVerticalStrut(20));
    }

    @Override
    protected void onConfirm() {
        String name = watchlistNameField.getText().trim();
        String type = (String) typeBox.getSelectedItem();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a name.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String currentUser = ClientUserSession.getInstance().getUsername();

        String visibilityToSend = type.toUpperCase().replace("-", "_");

        String command = "CREATE_LIST###" + currentUser + "###" + name + "###" + visibilityToSend;

        Object response = SocketManager.getInstance().sendRequest(command);

        if ("SUCCESS".equals(response)) {
            JOptionPane.showMessageDialog(this, "List '" + name + "' created successfully!");
            dispose();
        } else {
            String errorMsg = (response != null) ? response.toString() : "Connection error";
            JOptionPane.showMessageDialog(this, "Failed: " + errorMsg, "Server Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}