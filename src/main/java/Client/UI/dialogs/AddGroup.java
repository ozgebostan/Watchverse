package Client.UI.dialogs;

import Client.Network.SocketManager;
import Client.UI.utils.UIConstants;
import Client.UI.utils.UIMaker;
import Model.ClientUserSession;

import javax.swing.*;
import java.awt.*;

public class AddGroup extends BaseDialog {

    private JTextField groupName;

    public AddGroup(JFrame frame) {
        super(frame, new Dimension(400, 250) ,"Create Group", "Create");
    }

    @Override
    protected void addContent(Container container) {
        JLabel nameLabel = new JLabel("Group Name:");
        groupName = new JTextField();

        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        groupName.setAlignmentX(Component.LEFT_ALIGNMENT);


        UIMaker.styleLabel(nameLabel, Color.BLACK);
        UIMaker.styleField(groupName, false);

        container.setBackground(UIConstants.MAIN_APP_COLOR);
        container.add(Box.createVerticalGlue());
        container.add(nameLabel);
        container.add(Box.createVerticalStrut(10));
        container.add(groupName);
        container.add(Box.createVerticalGlue());
    }

    @Override
    protected void onConfirm() {
        String name = groupName.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Group name cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String username = ClientUserSession.getInstance().getUsername();

        String command = "CREATE_GROUP###" + username + "###" + name;

        Object response = SocketManager.getInstance().sendRequest(command);

        if ("SUCCESS".equals(response)) {
            JOptionPane.showMessageDialog(this, "Group '" + name + "' created successfully!");
            dispose();
        } else {
            String errorMsg = (response != null) ? response.toString() : "Connection failed";
            JOptionPane.showMessageDialog(this, "Failed to create group: " + errorMsg,
                    "Server Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}