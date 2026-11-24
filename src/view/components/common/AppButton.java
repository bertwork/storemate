package view.components.common;

import util.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class AppButton extends JButton {

    public AppButton(String label, ActionListener action) {
        super(label);
        addActionListener(action);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static AppButton tab(String label, ActionListener action) {
        AppButton b = new AppButton(label, action);
        b.putClientProperty("JButton.buttonType", "borderless");
        return b;
    }

    /** Borderless button that only shows an icon (e.g. remove). */
    public static AppButton icon(Icon icon, String tooltip, ActionListener action) {
        AppButton b = new AppButton(null, action);
        b.setIcon(icon);
        b.setToolTipText(tooltip);
        b.setFocusable(false);
        b.putClientProperty("JButton.buttonType", "borderless");
        return b;
    }

    /** Filled accent button (e.g. "Place Order"). */
    public static AppButton primary(String label, ActionListener action) {
        AppButton b = new AppButton(label, action);
        b.setFocusable(false);
        b.putClientProperty("FlatLaf.style", String.format(
                "background: %s; foreground: #FFFFFF; "
                        + "hoverBackground: #1D4ED8; pressedBackground: #1E40AF; "
                        + "disabledBackground: #E5E7EB; disabledText: #9CA3AF; "
                        + "arc: 12; font: bold",
                hex(Theme.ACCENT)));
        return b;
    }

    private static String hex(Color c) {
        return String.format("#%06X", c.getRGB() & 0xFFFFFF);
    }
}