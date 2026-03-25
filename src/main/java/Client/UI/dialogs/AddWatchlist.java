package Client.UI.dialogs;

import Client.Network.SocketManager;
import Client.UI.utils.UIConstants; // Renkler için
import Client.UI.utils.UIMaker;
import Model.ClientUserSession;

import javax.swing.*;
import java.awt.*;

public class AddWatchlist extends BaseDialog {

    private JTextField watchlistName;
    private JComboBox<String> typeBox;

    public AddWatchlist(JFrame frame) {
        super(frame, new Dimension(400, 350), "Create Watchlist", "Create");
    }

    @Override
    protected void addContent(Container container) {
        JLabel nameLabel = new JLabel("Watchlist Name");
        watchlistName = new JTextField();

        JLabel typeLabel = new JLabel("Visibility Type");
        typeBox = new JComboBox<>(new String[]{"Private", "Public", "Link-Only"});

        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        watchlistName.setAlignmentX(Component.LEFT_ALIGNMENT);
        typeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        typeBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        UIMaker.styleLabel(nameLabel, Color.BLACK);
        UIMaker.styleField(watchlistName, false);
        UIMaker.styleLabel(typeLabel, Color.BLACK);

        typeBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        typeBox.setBackground(Color.WHITE);

        // 3. Paneli İnşa Et
        container.add(nameLabel);
        container.add(Box.createVerticalStrut(8));
        container.add(watchlistName);

        container.add(Box.createVerticalStrut(20));

        container.add(typeLabel);
        container.add(Box.createVerticalStrut(8));
        container.add(typeBox);

        container.add(Box.createVerticalGlue());
    }

    @Override
    protected void onConfirm() {
        String name = watchlistName.getText().trim();
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