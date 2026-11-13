package view;

import util.AppInfo;
import util.Theme;
import view.components.RoundedPanel;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.Objects;

/**
 * Small borderless window shown while the application starts.
 * Displays the application logo, loading status, and progress indicator.
 */
public class SplashWindow extends JWindow {

    private static final String LOGO = "/resources/images/logo_full.png";
    private static final int LOGO_WIDTH = 260;

    private final JLabel statusLabel = new JLabel("Starting…", SwingConstants.CENTER);

    /**
     * Creates and initializes the splash window.
     */
    public SplashWindow() {
        // Use rounded corners when the current graphics environment supports transparency.
        boolean rounded = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .isWindowTranslucencySupported(
                        GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT
                );

        if (rounded) {
            setBackground(new Color(0, 0, 0, 0));
        }

        RoundedPanel card = new RoundedPanel(
                Theme.SURFACE,
                Theme.BORDER,
                rounded ? 24 : 0
        );

        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(BorderFactory.createEmptyBorder(36, 44, 24, 44));
        card.setPreferredSize(new Dimension(460, 280));

        card.add(new JLabel(loadLogo(), SwingConstants.CENTER), BorderLayout.CENTER);
        card.add(buildBottom(), BorderLayout.SOUTH);

        setContentPane(card);
        pack();
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
    }

    /**
     * Updates the loading status message.
     */
    public void setStatus(String text) {
        statusLabel.setText(Objects.requireNonNull(text, "Status text must not be null."));
    }

    /**
     * Creates the status, progress bar, and version section.
     */
    private JPanel buildBottom() {
        statusLabel.setFont(Theme.regular(statusLabel, 12f));
        statusLabel.setForeground(Theme.MUTED);

        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        bar.setBorderPainted(false);
        bar.setForeground(Theme.ACCENT);
        bar.setPreferredSize(new Dimension(0, 6));

        JLabel version = new JLabel("v" + AppInfo.VERSION, SwingConstants.CENTER);
        version.setFont(Theme.regular(version, 11f));
        version.setForeground(Theme.MUTED);

        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setOpaque(false);
        bottom.add(statusLabel, BorderLayout.NORTH);
        bottom.add(bar, BorderLayout.CENTER);
        bottom.add(version, BorderLayout.SOUTH);

        return bottom;
    }

    /**
     * Loads and scales the application logo.
     */
    private ImageIcon loadLogo() {
        URL logoUrl = Objects.requireNonNull(
                getClass().getResource(LOGO),
                "Splash logo not found: " + LOGO
        );

        ImageIcon raw = new ImageIcon(logoUrl);

        int originalWidth = raw.getIconWidth();
        int originalHeight = raw.getIconHeight();

        if (originalWidth <= 0 || originalHeight <= 0) {
            throw new IllegalStateException("Unable to load splash logo: " + LOGO);
        }

        int height = originalHeight * LOGO_WIDTH / originalWidth;

        Image scaled = raw.getImage().getScaledInstance(
                LOGO_WIDTH,
                height,
                Image.SCALE_SMOOTH
        );

        return new ImageIcon(scaled);
    }
}
