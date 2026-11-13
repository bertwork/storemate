package view.components.products;

import com.formdev.flatlaf.icons.FlatSearchIcon;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

/**
 * Search field with live text updates, submission, and clear shortcuts.
 */
public class SearchBar extends JTextField {

    public SearchBar(String placeholder, Consumer<String> onChange) {
        this(placeholder, onChange, () -> {});
    }

    public SearchBar(String placeholder, Consumer<String> onChange, Runnable onSubmit) {
        putClientProperty("JTextField.placeholderText", placeholder);
        putClientProperty("JTextField.leadingIcon", new FlatSearchIcon());
        putClientProperty("JTextField.showClearButton", true);
        setPreferredSize(new Dimension(0, 38));

        // Notify the listener whenever the search text changes.
        getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onChange.accept(getText().trim());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onChange.accept(getText().trim());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onChange.accept(getText().trim());
            }
        });

        // Submit the search when Enter is pressed.
        addActionListener(e -> onSubmit.run());

        // Clear the search when Escape is pressed.
        getInputMap(WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                "clear-search"
        );

        getActionMap().put("clear-search", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setText("");
            }
        });
    }
}