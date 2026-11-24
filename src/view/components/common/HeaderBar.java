package view.components.common;

import util.Theme;
import view.Screen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Application header containing the logo, screen navigation, and clock.
 */
public class HeaderBar extends JPanel {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH);

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

    private static final String APP_LOGO_FULL = "/resources/images/logo_full.png";

    private static final int IMAGE_HEIGHT = 36;

    private final JLabel dateLabel = new JLabel();
    private final JLabel timeLabel = new JLabel();
    private final Timer clockTimer = new Timer(1000, e -> updateClock());
    private final JComboBox<Screen> menu = new JComboBox<>(Screen.values());

    public HeaderBar(Consumer<Screen> onScreenChange) {
        setLayout(new BorderLayout());
        setBackground(Theme.SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(8, 12, 8, 16)
        ));

        add(buildLeft(onScreenChange), BorderLayout.WEST);
        add(buildClock(), BorderLayout.EAST);
    }

    // Starts the clock and registers shortcuts while the header is in a window.
    @Override
    public void addNotify() {
        super.addNotify();
        updateClock();
        clockTimer.start();
        registerShortcuts();
    }

    @Override
    public void removeNotify() {
        clockTimer.stop();
        super.removeNotify();
    }

    private JPanel buildLeft(Consumer<Screen> onScreenChange) {
        JLabel logo = new JLabel(loadLogo());
        logo.setAlignmentY(Component.CENTER_ALIGNMENT);

        menu.setSelectedItem(Screen.ORDER);
        menu.setFont(Theme.bold(menu, 14f));
        menu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        menu.putClientProperty("JComponent.roundRect", true);
        styleMenu();
        menu.setToolTipText("Switch screen (F1 Order, F2 Analytics)");
        menu.setPreferredSize(new Dimension(140, 36));
        menu.setMaximumSize(menu.getPreferredSize());
        menu.setAlignmentY(Component.CENTER_ALIGNMENT);

        menu.addActionListener(e ->
                onScreenChange.accept((Screen) menu.getSelectedItem())
        );

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));
        left.add(logo);
        left.add(Box.createHorizontalStrut(24));
        left.add(menu);

        return left;
    }

    // Applies the application's navy styling to the dropdown.
    private void styleMenu() {
        String navy = hex(Theme.NAVY);

        menu.putClientProperty("FlatLaf.style",
                "borderColor: #D1D5DB; " +
                        "focusedBorderColor: " + navy + "; " +
                        "focusColor: #C7D0E0; " +
                        "foreground: " + navy + "; " +
                        "buttonArrowColor: " + navy + "; " +
                        "buttonHoverArrowColor: " + navy + "; " +
                        "buttonPressedArrowColor: " + navy
        );

        menu.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean selected,
                    boolean hasFocus) {

                Component row = super.getListCellRendererComponent(
                        list, value, index, selected, hasFocus
                );

                if (index >= 0) {
                    row.setBackground(selected ? Theme.NAVY : Theme.SURFACE);
                    row.setForeground(selected ? Color.WHITE : Theme.NAVY);
                }

                return row;
            }
        });
    }

    private static String hex(Color color) {
        return String.format("#%06X", color.getRGB() & 0xFFFFFF);
    }

    // F1, F2, and subsequent function keys switch between screens.
    private void registerShortcuts() {
        JRootPane root = getRootPane();

        if (root == null) {
            return;
        }

        for (Screen screen : Screen.values()) {
            String key = "show-" + screen.name();

            root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(
                            KeyStroke.getKeyStroke(KeyEvent.VK_F1 + screen.ordinal(), 0),
                            key
                    );

            root.getActionMap().put(key, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    menu.setSelectedItem(screen);
                }
            });
        }
    }

    private JPanel buildClock() {
        dateLabel.setFont(Theme.bold(dateLabel, 14f));
        dateLabel.setForeground(Theme.NAVY);
        dateLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        timeLabel.setFont(Theme.regular(timeLabel, 12f));
        timeLabel.setForeground(Theme.MUTED);
        timeLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JPanel clock = new JPanel();
        clock.setOpaque(false);
        clock.setLayout(new BoxLayout(clock, BoxLayout.Y_AXIS));
        clock.add(dateLabel);
        clock.add(timeLabel);

        return clock;
    }

    private ImageIcon loadLogo() {
        ImageIcon raw = new ImageIcon(Objects.requireNonNull(getClass().getResource(APP_LOGO_FULL)));

        int width = raw.getIconWidth() * IMAGE_HEIGHT / raw.getIconHeight();

        return new ImageIcon(raw.getImage().getScaledInstance(width, IMAGE_HEIGHT, Image.SCALE_SMOOTH));
    }

    private void updateClock() {
        LocalDateTime now = LocalDateTime.now();

        dateLabel.setText(now.format(DATE_FORMAT));
        timeLabel.setText(now.format(TIME_FORMAT));
    }
}