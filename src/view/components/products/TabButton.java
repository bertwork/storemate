package view.components.products;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * Toggle button used to select a product category.
 */
public class TabButton extends JToggleButton {

    public TabButton(String label, ActionListener action) {
        super(label);

        addActionListener(action);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        putClientProperty("JButton.buttonType", "roundRect");

        // Make Enter behave like Space when pressing and releasing the button.
        InputMap im = getInputMap(WHEN_FOCUSED);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), "pressed");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true), "released");
    }
}
