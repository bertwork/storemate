package view.components.common;

import util.AppInfo;
import util.Theme;

import javax.swing.*;
import java.awt.*;

/**
 * Application footer displaying database connection status and version.
 */
public class FooterBar extends JPanel {

    private static final Color TEXT = new Color(0xD1D5DB);
    private static final Color OFFLINE_TEXT = new Color(0xFCA5A5);

    private final JLabel statusLabel = new JLabel();

    public FooterBar() {
        setLayout(new GridLayout(1, 3));
        setBackground(Theme.FOOTER_BG);
        setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        statusLabel.setFont(Theme.regular(statusLabel, 12f));
        statusLabel.setIconTextGap(6);
        showChecking();

        JLabel version = new JLabel(AppInfo.NAME + " v" + AppInfo.VERSION, SwingConstants.CENTER);
        version.setForeground(Color.WHITE);
        version.setFont(Theme.regular(version, 12f));

        add(statusLabel);
        add(version);
        add(new JLabel());
    }

    /**
     * Updates the database connection indicator.
     */
    public void setConnected(boolean connected) {
        statusLabel.setIcon(new DotIcon(10, connected ? Theme.SUCCESS : Theme.DANGER));
        statusLabel.setText(connected ? "Connected" : "Database offline");
        statusLabel.setForeground(connected ? TEXT : OFFLINE_TEXT);

        statusLabel.setToolTipText(connected ? "Database connection is working" : "Cannot reach the database. Is Laragon (MySQL) running?");
    }

    // Displays the initial connection-checking state.
    private void showChecking() {
        statusLabel.setIcon(new DotIcon(10, Theme.MUTED));
        statusLabel.setText("Checking…");
        statusLabel.setForeground(TEXT);
    }
}
