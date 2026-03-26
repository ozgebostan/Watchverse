package Client.UI.dialogs;

import Client.Network.SocketManager;
import Client.UI.utils.UIMaker;
import Client.UI.utils.UIConstants;
import Model.ClientUserSession;
import Model.Item;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AddMovieToListDialog extends BaseDialog {

    private final Item movie;
    private JComboBox<String> watchlistSelector;
    private JComboBox<String> prioritySelector;
    private JSpinner durationSpinner;

    public AddMovieToListDialog(JFrame frame, Item movie) {
        super(frame, new Dimension(400, 450), "Add Movie to Watchlist", "Add to List");
        this.movie = movie;

        loadUserWatchlists();
    }

    @Override
    protected void addContent(Container container) {
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        JLabel movieTitleLabel = new JLabel("<html><div style='text-align: center; width: 300px;'>" +
                "Adding <b>" + movie.title() + "</b> to your collection.</div></html>");
        movieTitleLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel listLabel = new JLabel("Choose Watchlist:");
        listLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        watchlistSelector = new JComboBox<>();
        watchlistSelector.setMaximumSize(new Dimension(300, 35));

        JLabel priorityLabel = new JLabel("Priority Level:");
        priorityLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        String[] priorities = {"Low (1)", "⚡ Medium (2)", "High (3)"};
        prioritySelector = new JComboBox<>(priorities);
        prioritySelector.setSelectedIndex(1);
        prioritySelector.setMaximumSize(new Dimension(300, 35));

        JLabel durationLabel = new JLabel("Duration (Minutes):");
        durationLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        int initialDuration = (movie.duration() > 0) ? movie.duration() : 120;
        durationSpinner = new JSpinner(new SpinnerNumberModel(initialDuration, 1, 1000, 5));
        durationSpinner.setMaximumSize(new Dimension(300, 35));

        container.setBackground(UIConstants.MAIN_APP_COLOR);
        container.add(Box.createVerticalStrut(10));
        container.add(movieTitleLabel);
        container.add(Box.createVerticalStrut(25));

        container.add(listLabel);
        container.add(Box.createVerticalStrut(5));
        container.add(watchlistSelector);

        container.add(Box.createVerticalStrut(20));

        container.add(priorityLabel);
        container.add(Box.createVerticalStrut(5));
        container.add(prioritySelector);

        container.add(Box.createVerticalStrut(20));

        container.add(durationLabel);
        container.add(Box.createVerticalStrut(5));
        container.add(durationSpinner);

        container.add(Box.createVerticalGlue());
    }

    private void loadUserWatchlists() {
        Object res = SocketManager.getInstance().sendRequest("GET_WATCHLISTS###" + ClientUserSession.getInstance().getUsername());
        if (res instanceof List) {
            List<String> listNames = (List<String>) res;
            for (String name : listNames) {
                watchlistSelector.addItem(name);
            }
        }
    }

    @Override
    protected void onConfirm() {
        String selectedListName = (String) watchlistSelector.getSelectedItem();

        int priorityValue = prioritySelector.getSelectedIndex() + 1;
        int durationValue = (int) durationSpinner.getValue();

        if (selectedListName == null) {
            JOptionPane.showMessageDialog(this, "Please select or create a watchlist first.");
            return;
        }

        // ADD_ITEM###USERNAME###LISTNAME###API_ID###TITLE###GENRES###POSTER_URL###PRIORITY###DURATION
        String cmd = String.format("ADD_ITEM###%s###%s###%s###%s###%s###%s###%d###%d",
                ClientUserSession.getInstance().getUsername(),
                selectedListName,
                movie.apiId(),
                movie.title(),
                movie.genres(),
                movie.posterUrl(),
                priorityValue,
                durationValue
        );

        Object response = SocketManager.getInstance().sendRequest(cmd);

        if ("SUCCESS".equals(response)) {
            JOptionPane.showMessageDialog(this, movie.title() + " successfully added!");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Error: " + response, "Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}