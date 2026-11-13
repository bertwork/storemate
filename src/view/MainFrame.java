package view;

import util.AppInfo;
import util.Theme;
import view.components.common.FooterBar;
import view.components.common.HeaderBar;
import view.panels.AnalyticsPanel;
import view.panels.OrderPanel;
import view.panels.TestPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.Objects;

/**
 * Main application window.
 * Manages the application layout and navigation between screens.
 */
public class MainFrame extends JFrame {

    private static final String APP_ICON_URL = "/resources/images/logo_icon.png";

    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 820;

    private final CardLayout screens = new CardLayout();
    private final JPanel content = new JPanel(screens);
    private final HeaderBar header = new HeaderBar(this::showScreen);
    private final FooterBar footer = new FooterBar();

    /**
     * Creates the main window and initializes its screens.
     */
    public MainFrame(OrderPanel orderPanel, AnalyticsPanel analyticsPanel, TestPanel testPanel) {
        Objects.requireNonNull(orderPanel, "OrderPanel must not be null.");
        Objects.requireNonNull(analyticsPanel, "AnalyticsPanel must not be null.");

        setTitle(AppInfo.NAME);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);

        initAppIcon();
        initUI(orderPanel, analyticsPanel, testPanel);
    }

    /**
     * Returns the footer bar for connection status updates.
     */
    public FooterBar getFooter() {
        return footer;
    }

    /**
     * Loads and applies the application icon.
     */
    private void initAppIcon() {
        URL iconUrl = Objects.requireNonNull(
                getClass().getResource(APP_ICON_URL),
                "Application icon not found: " + APP_ICON_URL
        );

        setIconImage(new ImageIcon(iconUrl).getImage());
    }

    /**
     * Initializes the main window layout and screen container.
     */
    private void initUI(OrderPanel orderPanel, AnalyticsPanel analyticsPanel, TestPanel testPanel) {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.APP_BG);

        content.setOpaque(false);
        content.add(orderPanel, Screen.ORDER.name());
        content.add(analyticsPanel, Screen.ANALYTICS.name());
        content.add(testPanel, Screen.PRODUCT.name());

        add(header, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        // Focus the search field when the window first opens.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                orderPanel.focusSearch();
            }
        });
    }

    /**
     * Displays the requested screen.
     */
    private void showScreen(Screen screen) {
        screens.show(content, Objects.requireNonNull(screen, "Screen must not be null.").name());
    }
}
