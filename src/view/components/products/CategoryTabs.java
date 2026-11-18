package view.components.products;

import javax.swing.*;
import java.util.function.Consumer;

/**
 * Displays the available product categories as selectable tabs.
 */
public class CategoryTabs extends JPanel {

    public CategoryTabs(String[] categories, Consumer<String> onSelect) {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        // Ensures that only one category tab can be selected at a time.
        ButtonGroup group = new ButtonGroup();

        for (String category : categories) {
            TabButton tab = new TabButton(category, e -> onSelect.accept(category));
            group.add(tab);
            tab.setSelected(group.getButtonCount() == 1);

            add(tab);
            add(Box.createHorizontalStrut(8));
        }
    }
}
