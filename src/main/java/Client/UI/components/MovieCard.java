package Client.UI.components;

import Model.Item;

import javax.swing.*;
import java.util.function.Consumer;

public class MovieCard extends JButton {

    private final Item item;

    public MovieCard(Item item, Consumer<Item> onDetailsClick) {
        this.item = item;

        setupUI();

        addActionListener(_ -> onDetailsClick.accept(item));
    }

    private void setupUI() {
    }
}
