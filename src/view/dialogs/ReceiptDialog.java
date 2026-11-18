package view.dialogs;

import model.Product;
import model.Receipt;
import util.Money;
import util.Theme;
import view.components.RoundedPanel;
import view.components.common.AppButton;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

/**
 * Displays the receipt after an order has been successfully saved.
 * Shows the purchased items, payment details, and change.
 */
public class ReceiptDialog extends JDialog {

    private static final DateTimeFormatter WHEN =
            DateTimeFormatter.ofPattern(
                    "MMMM d, yyyy  •  h:mm a",
                    Locale.ENGLISH
            );

    private static final int ITEMS_WIDTH = 340;
    private static final int ITEMS_MAX_HEIGHT = 240;

    /**
     * Opens the receipt dialog relative to the supplied component.
     */
    public static void show(Component parent, Receipt receipt) {
        new ReceiptDialog(
                SwingUtilities.getWindowAncestor(parent),
                receipt
        ).setVisible(true);
    }

    private ReceiptDialog(Window owner, Receipt receipt) {
        super(owner, "Order Receipt", ModalityType.APPLICATION_MODAL);

        AppButton done = AppButton.primary("Done", e -> dispose());
        done.setPreferredSize(new Dimension(0, 40));

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(Theme.SURFACE);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        content.add(buildHeader(receipt), BorderLayout.NORTH);
        content.add(buildItems(receipt), BorderLayout.CENTER);
        content.add(buildFooter(receipt, done), BorderLayout.SOUTH);

        setContentPane(content);

        // Enter closes the dialog.
        getRootPane().setDefaultButton(done);

        // Escape closes the dialog.
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        setResizable(false);
        pack();
        setLocationRelativeTo(owner);
    }

    // ---- Header ----

    private JPanel buildHeader(Receipt receipt) {
        JLabel title = new JLabel("✓  Order Placed");
        title.setFont(Theme.bold(title, 18f));
        title.setForeground(Theme.SUCCESS);

        JLabel number = new JLabel("Order #" + receipt.orderId());
        number.setFont(Theme.bold(number, 13f));
        number.setForeground(Theme.NAVY);

        JLabel when = new JLabel(receipt.placedAt().format(WHEN));
        when.setFont(Theme.regular(when, 12f));
        when.setForeground(Theme.MUTED);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(
                        0, 0, 1, 0, Theme.BORDER
                ),
                BorderFactory.createEmptyBorder(0, 0, 12, 0)
        ));

        for (JLabel label : new JLabel[]{title, number, when}) {
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            header.add(label);
        }

        header.add(Box.createVerticalStrut(0));

        return header;
    }

    // ---- Purchased items ----

    private JScrollPane buildItems(Receipt receipt) {
        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        for (Map.Entry<Product, Integer> line :
                receipt.items().entrySet()) {

            list.add(itemRow(line.getKey(), line.getValue()));
        }

        // Keeps each row at its natural height inside the scroll pane.
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(list, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        scroll.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );

        scroll.getVerticalScrollBar().setUnitIncrement(16);

        scroll.setPreferredSize(new Dimension(
                ITEMS_WIDTH,
                Math.min(
                        list.getPreferredSize().height,
                        ITEMS_MAX_HEIGHT
                )
        ));

        return scroll;
    }

    private JPanel itemRow(Product product, int quantity) {
        JLabel name = new JLabel(product.name());
        name.setFont(Theme.bold(name, 13f));
        name.setForeground(Theme.NAVY);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel detail = new JLabel(
                quantity + " × " + Money.format(product.price())
        );
        detail.setFont(Theme.regular(detail, 12f));
        detail.setForeground(Theme.MUTED);
        detail.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(name);
        text.add(detail);

        JLabel lineTotal = new JLabel(
                Money.format(
                        product.price().multiply(
                                BigDecimal.valueOf(quantity)
                        )
                )
        );
        lineTotal.setFont(Theme.bold(lineTotal, 13f));
        lineTotal.setForeground(Theme.NAVY);

        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));

        row.add(text, BorderLayout.CENTER);
        row.add(lineTotal, BorderLayout.EAST);

        return row;
    }

    // ---- Totals, payment, and button ----

    private JPanel buildFooter(Receipt receipt, AppButton done) {
        JPanel footer = new JPanel(new GridBagLayout());
        footer.setOpaque(false);

        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(12, 0, 0, 0)
        ));

        stack(
                footer,
                amountRow("Subtotal", Money.format(receipt.subtotal()), false),
                0
        );

        if (receipt.discount().signum() > 0) {
            String label = receipt.discountLabel().isBlank()
                    ? "Discount"
                    : "Discount (" + receipt.discountLabel() + ")";

            stack(
                    footer,
                    amountRow(
                            label,
                            "-" + Money.format(receipt.discount()),
                            false
                    ),
                    4
            );
        }

        stack(footer, new JSeparator(), 8);

        stack(
                footer,
                amountRow("Total", Money.format(receipt.total()), true),
                8
        );

        stack(
                footer,
                amountRow(
                        "Amount Received",
                        Money.format(receipt.amountReceived()),
                        false
                ),
                4
        );

        stack(footer, buildChangeBox(receipt), 10);
        stack(footer, done, 14);

        return footer;
    }

    private JPanel amountRow(String text, String amount, boolean strong) {
        JLabel label = new JLabel(text);
        label.setFont(strong ? Theme.bold(label, 15f) : Theme.regular(label, 13f));
        label.setForeground(strong ? Theme.NAVY : Theme.MUTED);

        JLabel value = new JLabel(amount);
        value.setFont(Theme.bold(value, strong ? 16f : 13f));
        value.setForeground(Theme.NAVY);

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        row.add(label, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);

        return row;
    }

    private RoundedPanel buildChangeBox(Receipt receipt) {
        JLabel title = new JLabel("Change");
        title.setFont(Theme.bold(title, 14f));
        title.setForeground(Theme.NAVY);

        JLabel amount = new JLabel(Money.format(receipt.change()));
        amount.setFont(Theme.bold(amount, 20f));
        amount.setForeground(Theme.SUCCESS);

        RoundedPanel box = new RoundedPanel(
                Theme.SUCCESS_BG,
                null,
                8
        );

        box.setLayout(new BorderLayout());
        box.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        box.add(title, BorderLayout.WEST);
        box.add(amount, BorderLayout.EAST);

        return box;
    }

    /**
     * Adds a component as a full-width row with spacing above it.
     */
    private static void stack(JPanel panel, JComponent component, int topGap) {
        GridBagConstraints gc = new GridBagConstraints();

        gc.gridx = 0;
        gc.gridy = panel.getComponentCount();
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(topGap, 0, 0, 0);

        panel.add(component, gc);
    }
}