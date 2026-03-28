package Client.UI.panels;

import Client.Network.SocketManager;
import Client.UI.dialogs.AddGroup;
import Client.UI.dialogs.AddMovieToListDialog;
import Client.UI.dialogs.AddWatchlist;
import Client.UI.frames.WatchverseFrame;
import Client.UI.utils.UIBehavior;
import Client.UI.utils.UIConstants;
import Client.UI.utils.UIMaker;
import Model.ClientUserSession;
import Model.Item;
import Model.PublicWatchlist;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.List;
import java.util.Objects;

public class WatchversePanel extends JPanel {
    private final WatchverseFrame frame;
    private final String SEARCH_HINT = "Search movies or shows...";
    private String currentViewedListName;
    private int currentViewedListId = -1;
    private JLabel welcomeLabel;
    private JTextField searchBar, discoverSearchBar;
    private JButton profileButton;
    private JList<String> watchlists, groups;
    private DefaultListModel<String> watchlistModel, groupModel;
    private JList<PublicWatchlist> publicWatchlists;
    private DefaultListModel<PublicWatchlist> publicListModel;
    private JPanel centerPanel, centerScreen, eastPanel;
    private CardLayout centerLayout, eastLayout;
    private JLabel detailPoster, detailTitle, detailGenre;
    private JLabel detailDuration, detailPriority, detailDate, detailYear;
    private JButton deleteButton;
    private Item currentSelectedItem;
    private JPopupMenu profileMenu;
    private JPopupMenu groupPopMenu;

    public WatchversePanel(WatchverseFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(UIConstants.MAIN_APP_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        setComponents();
        setComponentStyles();
        setComponentLayouts();
        setEvents();

        refreshWatchlists();
        refreshGroups();
        loadPublicWatchlists();
    }

    private void setComponents() {
        String currentUser = ClientUserSession.getInstance().getUsername();
        String formattedName = currentUser.substring(0, 1).toUpperCase() + currentUser.substring(1);

        //WEST PANEL COMPONENTS
        watchlistModel = new DefaultListModel<>();
        watchlists = new JList<>(watchlistModel);
        groupModel = new DefaultListModel<>();
        groups = new JList<>(groupModel);
        publicListModel = new DefaultListModel<>();
        publicWatchlists = new JList<>(publicListModel);

        watchlists.setAlignmentX(LEFT_ALIGNMENT);
        watchlists.setFont(UIConstants.LABEL_FONT);

        publicWatchlists.setAlignmentX(LEFT_ALIGNMENT);
        publicWatchlists.setFont(UIConstants.LABEL_FONT);

        groups.setAlignmentX(LEFT_ALIGNMENT);
        groups.setFont(UIConstants.LABEL_FONT);

        groupPopMenu = new JPopupMenu();
        JMenuItem addMyList = new JMenuItem("Add My Watchlist to Group");
        JMenuItem deleteGroup = new JMenuItem("Delete Group");
        JMenuItem createGroupCode = new JMenuItem("Get Group Code");

        deleteGroup.setFont(UIConstants.LINK_FONT);
        createGroupCode.setFont(UIConstants.LINK_FONT);
        addMyList.setFont(UIConstants.LINK_FONT);

        groupPopMenu.add(deleteGroup);
        groupPopMenu.add(createGroupCode);
        groupPopMenu.add(addMyList);


        //Listeners for group menu items
        deleteGroup.addActionListener(_ -> {
            String selected = groups.getSelectedValue();
            if (selected != null) {
                new Thread(() -> {
                    Object res = request("DELETE_GROUP###" + currentUser + "###" + selected);
                    if ("SUCCESS".equals(res)) refreshGroups();
                }).start();
            }
        });

        createGroupCode.addActionListener(_ -> {
            String selected = groups.getSelectedValue();
            if (selected != null) {
                new Thread(() -> {
                    Object res = request("GET_GROUP_CODE###" + currentUser + "###" + selected);
                    if (res instanceof String) {
                        SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(null, "Group Code: " + res));
                    }
                }).start();
            }
        });

        addMyList.addActionListener(_ -> {
            String selectedGroup = groups.getSelectedValue();
            if (selectedGroup != null) {
                // 1. Kullanıcının kendi listelerini çek
                Object res = request("GET_WATCHLISTS###" + ClientUserSession.getInstance().getUsername());
                if (res instanceof List<?> myLists && !myLists.isEmpty()) {
                    // 2. Seçim kutusu göster
                    String choice = (String) JOptionPane.showInputDialog(null,
                            "Select one of your lists to link to '" + selectedGroup + "':",
                            "Link Watchlist", JOptionPane.QUESTION_MESSAGE, null,
                            myLists.toArray(), myLists.toArray()[0]);

                    if (choice != null) {
                        new Thread(() -> {
                            // 3. Gruba bağlama isteği at
                            Object status = request("ADD_LIST_TO_GROUP###" +
                                    ClientUserSession.getInstance().getUsername() +
                                    "###" + selectedGroup + "###" + choice);

                            if ("SUCCESS".equals(status)) {
                                JOptionPane.showMessageDialog(null, "List linked successfully!");
                                // Grubu otomatik yenilemek için tıklamayı tetikle
                                groups.setSelectedValue(selectedGroup, true);
                            } else {
                                String msg = status.toString().contains("PRIVATE") ?
                                        "Private lists cannot be shared in groups!" : "Error: " + status;
                                JOptionPane.showMessageDialog(null, msg);
                            }
                        }).start();
                    }
                }
            }
        });

        discoverSearchBar = new JTextField("Discover other watchlists...");
        UIMaker.styleField(discoverSearchBar, true);

        discoverSearchBar.setMaximumSize(new Dimension(330, 35));
        discoverSearchBar.setPreferredSize(new Dimension(330 , 35));

        discoverSearchBar.setAlignmentX(LEFT_ALIGNMENT);

        //NORTH PANEL COMPONENTS
        welcomeLabel = new JLabel("Welcome " + formattedName);

        searchBar = new JTextField(SEARCH_HINT);
        UIMaker.styleField(searchBar, true);
        searchBar.setPreferredSize(new Dimension(740, 40));

        profileButton = new JButton(String.valueOf(formattedName.charAt(0)));
        profileButton.setFocusPainted(false);
        profileButton.setBorderPainted(false);
        profileButton.setContentAreaFilled(false);
        profileButton.setOpaque(true);
        profileButton.setFont(UIConstants.LABEL_FONT);

        // Profile Menu
        profileMenu = new JPopupMenu();
        profileMenu.setPopupSize(120, 100);

        JMenuItem settingsItem = new JMenuItem("Settings");
        settingsItem.setFont(UIConstants.LINK_FONT);
        settingsItem.setForeground(UIConstants.LINK_COLOR);

        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.setFont(UIConstants.LINK_FONT);
        logoutItem.setForeground(UIConstants.LINK_COLOR);

        settingsItem.addActionListener(_ -> frame.openSettings());
        logoutItem.addActionListener(_ -> frame.logout());
        profileMenu.add(settingsItem);
        profileMenu.addSeparator();
        profileMenu.add(logoutItem);

        // Details Panel items, EAST PANEL
        detailPoster = new JLabel("", SwingConstants.CENTER);
        detailPoster.setAlignmentX(CENTER_ALIGNMENT);
        detailPoster.setPreferredSize(new Dimension(220, 330));

        detailTitle = new JLabel("Select an Item", SwingConstants.CENTER);
        detailTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        detailTitle.setAlignmentX(CENTER_ALIGNMENT);

        detailGenre = new JLabel("", SwingConstants.CENTER);
        detailGenre.setForeground(Color.GRAY);
        detailGenre.setAlignmentX(CENTER_ALIGNMENT);

        deleteButton = new JButton("Remove Item");
        UIMaker.styleButton(deleteButton, UIConstants.COMP_SIZE, UIConstants.DELETE);


    }

    private Object request(String cmd) {
        return SocketManager.getInstance().sendRequest(cmd);
    }

    public void refreshWatchlists() {
        new Thread(() -> {
            Object res = request("GET_WATCHLISTS###" + ClientUserSession.getInstance().getUsername());
            if (res instanceof List) {
                SwingUtilities.invokeLater(() -> {
                    watchlistModel.clear();
                    ((List<String>) res).forEach(watchlistModel::addElement);
                });
            }
        }).start();
    }

    public void refreshGroups() {
        new Thread(() -> {
            Object res = request("GET_MY_GROUPS###" + ClientUserSession.getInstance().getUsername());
            if (res instanceof List) {
                SwingUtilities.invokeLater(() -> {
                    groupModel.clear();
                    ((List<String>) res).forEach(groupModel::addElement);
                });

            }
        }).start();
    }

    private void loadWatchlist(String listName) {
        this.currentViewedListName = listName;
        this.currentViewedListId = -1;
        Object res = request("GET_LIST_ITEMS###" + ClientUserSession.getInstance().getUsername() + "###" + listName);
        if (res instanceof List) displayItems((List<Item>) res);
    }

    private void loadPublicWatchlists() {
        new Thread(() -> {
            Object res = request("GET_PUBLIC_LISTS");
            if (res instanceof List) {
                SwingUtilities.invokeLater(() -> {
                    publicListModel.clear();
                    ((List<PublicWatchlist>) res).forEach(publicListModel::addElement);
                });
            }
        }).start();
    }

    private void loadPublicListItems(int listId, String listName) {
        this.currentViewedListId = listId;
        this.currentViewedListName = listName;
        Object res = request("GET_PUBLIC_LIST_ITEMS###" + listId);
        if (res instanceof List) displayItems((List<Item>) res);
    }

    private void displayItems(List<Item> items) {
        SwingUtilities.invokeLater(() -> {
            centerScreen.removeAll();

            if (items != null && !items.isEmpty()) {
                items.sort((a, b) -> {
                    double scoreA = (double) a.priority() / Math.max(1, a.duration());
                    double scoreB = (double) b.priority() / Math.max(1, b.duration());

                    int comparison = Double.compare(scoreB, scoreA);

                    return (comparison == 0) ? a.title().compareToIgnoreCase(b.title()) : comparison;
                });

                for (Item item : items) {
                    centerScreen.add(createMovieCard(item));
                }
                centerLayout.show(centerPanel, "CONTENT");
            } else {
                centerLayout.show(centerPanel, "EMPTY");
            }
            centerScreen.revalidate();
            centerScreen.repaint();
        });
    }

    private void displayWatchlistFolders(List<PublicWatchlist> folders) {
        SwingUtilities.invokeLater(() -> {
            centerScreen.removeAll();
            centerScreen.setLayout(new GridLayout(0, 4, 15, 15));

            if (folders != null && !folders.isEmpty()) {
                for (PublicWatchlist folder : folders) {
                    JButton folderBtn = new JButton(folder.name());

                    UIMaker.styleButton(folderBtn, new Dimension(180, 100), UIConstants.ADD_BUTTON_COLOR);

                    folderBtn.addActionListener(_ -> {
                        new Thread(() -> {
                            Object res = request("GET_PUBLIC_LIST_ITEMS###" + folder.id());
                            if (res instanceof List) {
                                displayItems((List<Item>) res);
                                currentViewedListId = folder.id();
                            }
                        }).start();
                    });
                    centerScreen.add(folderBtn);
                }
                centerLayout.show(centerPanel, "CONTENT");
            } else {
                centerLayout.show(centerPanel, "EMPTY");
            }
            centerScreen.revalidate();
            centerScreen.repaint();
        });
    }

    private JButton createMovieCard(Item item) {
        //Styling
        JButton card = new JButton();
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(180, 280));
        card.setBackground(Color.WHITE);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setVerticalTextPosition(SwingConstants.BOTTOM);
        card.setHorizontalTextPosition(SwingConstants.CENTER);


        //Poster loading
        ImageIcon whiteIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/white.jpg")));
        Image whiteImg = whiteIcon.getImage().getScaledInstance(200, 230, Image.SCALE_SMOOTH);
        card.setIcon(new ImageIcon(whiteImg));

        if (item.posterUrl() != null && !item.posterUrl().contains("null")) {
            new Thread(() -> {
                try {
                    URL url = new URL(item.posterUrl());
                    ImageIcon icon = new ImageIcon(url);

                    if (icon.getIconWidth() > 0) {
                        Image img = icon.getImage().getScaledInstance(200, 230, Image.SCALE_SMOOTH);
                        SwingUtilities.invokeLater(() -> card.setIcon(new ImageIcon(img)));
                    }
                } catch (Exception ignore) {
                    //no poster
                }
            }).start();
        }
        String labelText = "<html><center><b>" + item.title() + "</b><br>"
                + "<font color='gray'>" + item.genres() + "</font></center></html>";

        card.setText(labelText);

        card.addActionListener(_ -> {
            //for details panel after adding item to the list
            if (currentViewedListName != null && !currentViewedListName.isEmpty()) {
                showItemDetails(item);
            } else {
                new AddMovieToListDialog(frame, item).setVisible(true);
            }
        });

        return card;
    }

    private void showItemDetails(Item item) {
        this.currentSelectedItem = item;
        detailTitle.setText("<html><center style='width:200px;'>" + item.title() + "</center></html>");
        detailGenre.setText(item.genres());

        String durationText = (item.duration() > 0) ? item.duration() + " Minutes" : "Duration Unknown";
        detailDuration.setText("<html><b>Duration:</b> " + durationText + "</html>");

        String priorityText = (item.priority() == 3) ? "High" : (item.priority() == 2) ? "Medium" : "Low";
        detailPriority.setText("<html><b>Priority:</b> " + priorityText + "</html>");

        String yearText = (item.releaseYear() > 0) ? String.valueOf(item.releaseYear()) : "N/A";
        detailYear.setText("<html><center><b>Released:</b> " + yearText + "</center></html>");

        String dateText = (item.addedDate() != null) ? item.addedDate() : "Unknown";
        detailDate.setText("<html><center><font color='gray' size='3'>Added on: " + dateText + "</font></center></html>");

        deleteButton.setVisible(currentViewedListId == -1);

        new Thread(() -> {
            try {
                SwingUtilities.invokeLater(() -> {
                    detailPoster.setIcon(null);
                    detailPoster.setText("Loading...");
                });

                if (item.posterUrl() != null && !item.posterUrl().isEmpty()) {
                    ImageIcon icon = new ImageIcon(new java.net.URL(item.posterUrl()));
                    Image scaledImg = icon.getImage().getScaledInstance(220, 330, Image.SCALE_SMOOTH);
                    SwingUtilities.invokeLater(() -> {
                        detailPoster.setText("");
                        detailPoster.setIcon(new ImageIcon(scaledImg));
                    });
                } else {
                    SwingUtilities.invokeLater(() -> detailPoster.setText("No Poster"));
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> detailPoster.setText("Error"));
            }
        }).start();

        eastLayout.show(eastPanel, "ITEM_DETAILS");
    }

    private void buildNorthPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(welcomeLabel, BorderLayout.WEST);

        JPanel searchContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchContainer.setOpaque(false);
        searchContainer.add(searchBar);
        header.add(searchContainer, BorderLayout.CENTER);

        JPanel profileContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        profileContainer.setOpaque(false);
        profileContainer.add(profileButton);
        header.add(profileContainer, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    private void buildWestPanel() {
        JPanel west = new JPanel();
        west.setLayout(new BoxLayout(west, BoxLayout.Y_AXIS));
        west.setPreferredSize(new Dimension(330, 0));
        west.setOpaque(false);
        west.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 15));

        // Watchlists
        west.add(titleWithAdButton("My Watchlists", () -> {
            new AddWatchlist(frame).setVisible(true);
            refreshWatchlists();
        }, "Add new list", "+", true));
        west.add(Box.createVerticalStrut(10));

        JScrollPane watchlistScroll = new JScrollPane(watchlists);
        watchlistScroll.setAlignmentX(LEFT_ALIGNMENT);
        west.add(watchlistScroll);

        // Groups
        west.add(Box.createVerticalStrut(30));
        west.add(titleWithAdButton("Groups", this::handleGroupOptions, "Manage groups","+", true));
        west.add(Box.createVerticalStrut(10));

        JScrollPane groupScroll = new JScrollPane(groups);
        groupScroll.setAlignmentX(LEFT_ALIGNMENT);
        west.add(groupScroll);

        // Discover
        west.add(Box.createVerticalStrut(30));
        JPanel discoverHeader = titleWithAdButton("Discover", () -> System.out.println("Searching public lists..."), "Search public lists", "", false);


        west.add(discoverHeader);
        west.add(Box.createVerticalStrut(10));
        west.add(discoverSearchBar);

        west.add(Box.createVerticalStrut(10));

        JScrollPane discoverScroll = new JScrollPane(publicWatchlists);
        discoverScroll.setAlignmentX(LEFT_ALIGNMENT);
        west.add(discoverScroll);

        add(west, BorderLayout.WEST);
    }

    private void handleGroupOptions() {
        Object[] options = {"Create Group", "Join with Code"};
        int choice = JOptionPane.showOptionDialog(frame, "Group Management", "Groups",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (choice == JOptionPane.YES_OPTION) {
            new AddGroup(frame).setVisible(true);
            refreshGroups();
        } else if (choice == JOptionPane.NO_OPTION) {
            String code = JOptionPane.showInputDialog(frame, "Enter invite code:");
            if (code != null && !code.trim().isEmpty()) {
                Object res = request("JOIN_GROUP###" + ClientUserSession.getInstance().getUsername() + "###" + code.trim());
                if (res != null && res.toString().startsWith("SUCCESS")) refreshGroups();
                else JOptionPane.showMessageDialog(frame, "Invalid code or error.");
            }
        }
    }

    private void buildCenterPanel() {
        centerLayout = new CardLayout();
        centerPanel = new JPanel(centerLayout);
        centerPanel.setOpaque(false);

        //EMPTY STATE
        JPanel emptyState = new JPanel(new GridBagLayout());
        emptyState.setOpaque(false);
        JLabel label = new JLabel("Select a list or search content in searchbar to see contents");
        UIMaker.styleLabel(label, UIConstants.HINT_GRAY);
        label.setFont(new Font("Segoe UI", Font.BOLD, 30));
        emptyState.add(label);

        //LOADING STATE
        JPanel loadingState = new JPanel(new GridBagLayout());
        loadingState.setOpaque(false);
        try {
            ImageIcon loadingGif = new ImageIcon(Objects.requireNonNull(getClass().getResource("/loading.gif")));
            loadingState.add(new JLabel(loadingGif));
        } catch (Exception e) {
            JLabel loadingLabel = new JLabel("Searching... Please wait.");
            UIMaker.styleLabel(loadingLabel, UIConstants.HINT_GRAY);
            loadingState.add(loadingLabel);
        }

        //CONTENT STATE
        centerScreen = new JPanel(new GridLayout(0, 4, 15, 15));
        centerScreen.setOpaque(false);

        JPanel aligner = new JPanel(new BorderLayout());
        aligner.setOpaque(false);
        aligner.add(centerScreen, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(aligner);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        centerPanel.add(emptyState, "EMPTY");
        centerPanel.add(loadingState, "LOADING");
        centerPanel.add(scroll, "CONTENT");

        add(centerPanel, BorderLayout.CENTER);
    }

    private void buildEastPanel() {
        eastLayout = new CardLayout();
        eastPanel = new JPanel(eastLayout);
        eastPanel.setOpaque(false);
        eastPanel.setPreferredSize(new Dimension(280, 0));

        JPanel itemDetailPanel = new JPanel();
        itemDetailPanel.setLayout(new BoxLayout(itemDetailPanel, BoxLayout.Y_AXIS));
        itemDetailPanel.setOpaque(false);
        itemDetailPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        itemDetailPanel.add(detailPoster);
        itemDetailPanel.add(Box.createVerticalStrut(20));
        itemDetailPanel.add(detailTitle);
        itemDetailPanel.add(Box.createVerticalStrut(10));
        itemDetailPanel.add(detailGenre);
        itemDetailPanel.add(Box.createVerticalStrut(10));

        detailDuration = new JLabel();
        detailDuration.setAlignmentX(CENTER_ALIGNMENT);
        detailDuration.setForeground(Color.DARK_GRAY);

        detailPriority = new JLabel();
        detailPriority.setAlignmentX(CENTER_ALIGNMENT);
        detailPriority.setForeground(Color.DARK_GRAY);


        detailYear = new JLabel("", SwingConstants.CENTER);
        detailYear.setAlignmentX(CENTER_ALIGNMENT);
        detailYear.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        detailDate = new JLabel("", SwingConstants.CENTER);
        detailDate.setAlignmentX(CENTER_ALIGNMENT);
        detailDate.setForeground(Color.DARK_GRAY);

        itemDetailPanel.add(detailYear);
        itemDetailPanel.add(Box.createVerticalStrut(10));

        itemDetailPanel.add(detailDuration);
        itemDetailPanel.add(detailPriority);

        itemDetailPanel.add(Box.createVerticalStrut(20));
        itemDetailPanel.add(detailDate);

        itemDetailPanel.add(Box.createVerticalStrut(30));
        itemDetailPanel.add(deleteButton);


        eastPanel.add(new JPanel(), "EMPTY");
        eastPanel.add(itemDetailPanel, "ITEM_DETAILS");

        add(eastPanel, BorderLayout.EAST);
    }

    private JPanel titleWithAdButton(String title, Runnable onAdd, String help, String btnText, boolean isVisible) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(UIConstants.COMP_SIZE.width, 35));
        panel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JButton addBtn = new JButton(btnText);
        addBtn.setForeground(UIConstants.ADD_BUTTON_COLOR);

        panel.setMaximumSize(new Dimension(330, 35));
        panel.setPreferredSize(new Dimension(330, 35));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        addBtn.setVisible(isVisible);

        addBtn.setToolTipText(help);
        addBtn.setFocusPainted(false);
        addBtn.addActionListener(_ -> onAdd.run());

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(addBtn, BorderLayout.EAST);
        return panel;
    }

    private void performMovieSearch(String query) {
        this.currentViewedListName = null;

        centerLayout.show(centerPanel, "LOADING");

        new Thread(() -> {
            try {
                Object res = request("SEARCH###" + query + "###movie");

                SwingUtilities.invokeLater(() -> {
                    centerScreen.removeAll();

                    if (res instanceof List<?> results && !results.isEmpty()) {
                        for (Object obj : results) {
                            if (obj instanceof Item item) {
                                JButton card = createMovieCard(item);
                                centerScreen.add(card);
                            }
                        }
                        centerLayout.show(centerPanel, "CONTENT");
                    } else {
                        centerLayout.show(centerPanel, "EMPTY");
                    }
                    centerScreen.revalidate();
                    centerScreen.repaint();
                });

            } catch (Exception e) {
                System.err.println("[ERROR] Search error: " + e.getMessage());
                SwingUtilities.invokeLater(() -> centerLayout.show(centerPanel, "EMPTY"));
            }
        }).start();
    }

    private void setEvents() {
        UIBehavior.setTextFieldPlaceholder(searchBar, SEARCH_HINT);
        UIBehavior.setTextFieldPlaceholder(discoverSearchBar, "Search public lists...");

        profileButton.addActionListener(_ -> {
            int menuWidth = profileMenu.getPreferredSize().width;
            int x = profileButton.getWidth() - menuWidth;
            int y = profileButton.getHeight();

            profileMenu.show(profileButton, x, y);
        });

        // Watchlist Events
        watchlists.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int index = watchlists.locationToIndex(e.getPoint());
                if (index < 0) return;

                String selected = watchlists.getSelectedValue();
                if (SwingUtilities.isLeftMouseButton(e)) {
                    groups.clearSelection();
                    publicWatchlists.clearSelection();
                    loadWatchlist(selected);
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    watchlists.setSelectedIndex(index);
                    JPopupMenu watchlistMenu = new JPopupMenu();
                    JMenuItem deleteWatchlist = new JMenuItem("Delete List");
                    deleteWatchlist.setFont(UIConstants.LINK_FONT);
                    deleteWatchlist.addActionListener(_ -> {
                        if (request("DELETE_LIST###" + ClientUserSession.getInstance().getUsername() + "###" + selected).equals("SUCCESS"))
                            refreshWatchlists();
                    });
                    watchlistMenu.add(deleteWatchlist);
                    watchlistMenu.show(watchlists, e.getX(), e.getY());
                }
            }
        });

        //group events
        groups.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && groups.getSelectedValue() != null) {
                // Diğer seçimleri temizle
                watchlists.clearSelection();
                publicWatchlists.clearSelection();

                String groupName = groups.getSelectedValue();
                String username = ClientUserSession.getInstance().getUsername();

                // 1. Önce "Yükleniyor" ekranını göster (Anlık geri bildirim için önemli)
                centerLayout.show(centerPanel, "LOADING");

                new Thread(() -> {
                    // Veriyi çek
                    Object res = request("GET_GROUP_WATCHLISTS_OBJECTS###" + username + "###" + groupName);

                    if (res instanceof List) {
                        // 2. UI Güncellemesini Swing Thread'ine (EDT) geri gönder
                        SwingUtilities.invokeLater(() -> {
                            displayWatchlistFolders((List<PublicWatchlist>) res);

                            // 3. ZORLA YENİDEN ÇİZ (Güncellenmeme sorununu bu çözer)
                            centerScreen.revalidate();
                            centerScreen.repaint();
                            centerPanel.revalidate();
                            centerPanel.repaint();

                            // 4. İçeriği göster
                            centerLayout.show(centerPanel, "CONTENT");
                        });
                    } else {
                        // Eğer hata dönerse veya liste boşsa boş ekranı göster
                        SwingUtilities.invokeLater(() -> centerLayout.show(centerPanel, "EMPTY"));
                    }
                }).start();
            }
        });

// Sağ tık (Pop-up Menu) kısmı değişmiyor, o doğru çalışıyor
        groups.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int index = groups.locationToIndex(e.getPoint());
                    if (index != -1) {
                        groups.setSelectedIndex(index);
                        groupPopMenu.show(groups, e.getX(), e.getY());
                    }
                }
            }
        });

        searchBar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) { // Enter'a basıldığında
                    String query = searchBar.getText().trim();
                    if (!query.isEmpty() && !query.equals(SEARCH_HINT)) {
                        performMovieSearch(query);
                    }
                }
            }
        });
        // Search/Discover Event
        discoverSearchBar.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String q = discoverSearchBar.getText().toLowerCase().trim();
                if (q.isEmpty() || q.equals("search public lists...")) {
                    publicWatchlists.setModel(publicListModel);
                } else {
                    DefaultListModel<PublicWatchlist> filtered = new DefaultListModel<>();
                    for (int i = 0; i < publicListModel.size(); i++) {
                        if (publicListModel.get(i).name().toLowerCase().contains(q))
                            filtered.addElement(publicListModel.get(i));
                    }
                    publicWatchlists.setModel(filtered);
                }
            }
        });

        publicWatchlists.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && publicWatchlists.getSelectedValue() != null) {
                watchlists.clearSelection();
                groups.clearSelection();

                PublicWatchlist selected = publicWatchlists.getSelectedValue();
                loadPublicListItems(selected.id(), selected.name());
            }
        });

        // Delete Button Event
        deleteButton.addActionListener(_ -> {
            if (currentSelectedItem == null || currentViewedListName == null) return;
            int confirm = JOptionPane.showConfirmDialog(frame, "Delete '" + currentSelectedItem.title() + "'?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (request("REMOVE_ITEM###" + ClientUserSession.getInstance().getUsername() + "###" + currentViewedListName + "###" + currentSelectedItem.apiId()).equals("SUCCESS")) {
                    loadWatchlist(currentViewedListName);
                    eastLayout.show(eastPanel, "EMPTY");
                }
            }
        });

    }

    private void setComponentStyles() {
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        profileButton.setPreferredSize(new Dimension(45, 45));
        profileButton.setBackground(new Color(60, 60, 60));
        profileButton.setForeground(Color.WHITE);
        profileButton.setFocusPainted(false);
        profileButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void setComponentLayouts() {
        buildNorthPanel();
        buildWestPanel();
        buildCenterPanel();
        buildEastPanel();
    }


}