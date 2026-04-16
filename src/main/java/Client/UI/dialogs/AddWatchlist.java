package Client.UI.dialogs;

import Client.Network.SocketManager;
import Client.UI.utils.UIConstants;
import Client.UI.utils.UIMaker;
import Model.ClientUserSession;

import javax.swing.*;
import java.awt.*;


public class AddWatchlist extends BaseDialog {

    private JTextField watchlistNameField;
    private JComboBox<String> typeBox;

    public AddWatchlist(JFrame frame) {
        super(frame, new Dimension(400, 350), "Create Watchlist", "Create");
        buildUI();
    }

    @Override
    protected void addContent(Container container) {
        //elements of the dialog
        JLabel watchlistNameLabel = new JLabel("Watchlist Name:");
        watchlistNameField = new JTextField();

        JLabel typeLabel = new JLabel("Visibility Type:");
        typeBox = new JComboBox<>(new String[]{"Private", "Public", "Link-Only"});
        typeBox.setFont(UIConstants.LABEL_FONT);


        //Wrappers that will be used in add watchlist dialog
        JPanel compWrapper = new JPanel();
        compWrapper.setOpaque(false);
        compWrapper.setLayout(new BoxLayout(compWrapper, BoxLayout.Y_AXIS));

        //watchlist name wrapper
        JPanel watchlistNameWrapper = new JPanel();
        watchlistNameWrapper.setOpaque(false);
        watchlistNameWrapper.setLayout(new BoxLayout(watchlistNameWrapper, BoxLayout.X_AXIS));

        UIMaker.styleLabel(watchlistNameLabel, Color.BLACK);
        UIMaker.styleField(watchlistNameField, false);

        watchlistNameWrapper.add(watchlistNameLabel);
        watchlistNameWrapper.add(Box.createHorizontalStrut(10));
        watchlistNameWrapper.add(watchlistNameField);


        //watchlist type wrapper
        JPanel typeWrapper = new JPanel();
        typeWrapper.setOpaque(false);
        typeWrapper.setLayout(new BoxLayout(typeWrapper, BoxLayout.X_AXIS));

        typeBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        UIMaker.styleLabel(typeLabel, Color.BLACK);

        typeBox.setMaximumSize(UIConstants.COMP_SIZE);
        typeBox.setBackground(Color.WHITE);

        typeWrapper.add(typeLabel);
        typeWrapper.add(Box.createHorizontalStrut(10));
        typeWrapper.add(typeBox);

        compWrapper.add(watchlistNameWrapper);
        compWrapper.add(Box.createVerticalStrut(15));
        compWrapper.add(typeWrapper);

        container.setBackground(UIConstants.MAIN_APP_COLOR);
        container.add(compWrapper);

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