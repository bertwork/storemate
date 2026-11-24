package view.components.checkout;

import util.Theme;

import javax.swing.*;
import java.awt.*;
import java.util.function.IntConsumer;

/**
 * Provides controls for increasing or decreasing a product quantity.
 */
public class QuantityStepper extends JPanel {

    private int quantity;
    private final JLabel quantityLabel = new JLabel();

    public QuantityStepper(int start, IntConsumer onChange) {
        this.quantity = start;

        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        JButton minus = smallButton("−");
        JButton plus = smallButton("+");

        fixSize(quantityLabel, 28, 32);
        quantityLabel.setFont(Theme.bold(quantityLabel, 14f));
        quantityLabel.setHorizontalAlignment(SwingConstants.CENTER);
        refresh();

        // Notify the listener whenever the quantity changes.
        minus.addActionListener(e -> {
            quantity--;
            refresh();
            onChange.accept(quantity);
        });

        plus.addActionListener(e -> {
            quantity++;
            refresh();
            onChange.accept(quantity);
        });

        add(minus);
        add(Box.createHorizontalStrut(4));
        add(quantityLabel);
        add(Box.createHorizontalStrut(4));
        add(plus);
    }

    private JButton smallButton(String text) {
        JButton button = new JButton(text);
        fixSize(button, 32, 32);

        button.setFont(Theme.bold(button, 16f));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setFocusable(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return button;
    }

    // Keeps the preferred, minimum, and maximum dimensions consistent.
    private static void fixSize(JComponent component, int width, int height) {
        Dimension size = new Dimension(width, height);

        component.setPreferredSize(size);
        component.setMinimumSize(size);
        component.setMaximumSize(size);
    }

    private void refresh() {
        quantityLabel.setText(String.valueOf(quantity));
    }
}