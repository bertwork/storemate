package view.panels;

import model.Product;
import util.Money;
import util.Theme;
import view.components.common.AppButton;
import view.components.checkout.CheckoutItemRow;
import view.components.RoundedPanel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Checkout panel containing the order summary, payment input,
 * quick-cash buttons, and order actions.
 * This class handles presentation only.
 * Order operations are delegated to the controller through callbacks.
 */
public class CheckoutPanel extends RoundedPanel {

    private static final String EMPTY = "empty";
    private static final String ITEMS = "items";

    // ---- item list ----

    private final CardLayout centerLayout = new CardLayout();
    private final JPanel center = new JPanel(centerLayout);
    private final JPanel itemList = new JPanel();

    // ---- order summary ----

    private final JLabel subtotalLabel = new JLabel();
    private final JLabel discountLabel = new JLabel();
    private final JLabel totalLabel = new JLabel();

    // ---- payment ----

    private final JTextField receivedField = new JTextField();

    private final RoundedPanel changeBox =
            new RoundedPanel(Theme.SUCCESS_BG, null, 8);

    private final JLabel changeTitle = new JLabel("Change");
    private final JLabel changeAmount = new JLabel();

    private final AppButton placeOrderButton;

    // ---- empty-state handling ----

    private final AppButton clearButton;

    private JPanel footer;
    private JPanel quickCash;

    private int itemCount = 0;

    private BigDecimal total = BigDecimal.ZERO;

    // True while an order is being saved.
    private boolean busy = false;

    public CheckoutPanel(Runnable onClear, Runnable onPlaceOrder) {
        super(Theme.SURFACE, Theme.BORDER, 16);

        Objects.requireNonNull(onClear, "Clear action cannot be null.");
        Objects.requireNonNull(onPlaceOrder, "Place order action cannot be null.");

        placeOrderButton = AppButton.primary(
                "Place Order",
                e -> onPlaceOrder.run()
        );

        clearButton = new AppButton(
                "Clear",
                e -> confirmClear(onClear)
        );

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(380, 0));

        add(buildTop(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        setSummary(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        showEmptyState(true);
    }

    // ---- controller -> view updates ----

    /**
     * Rebuilds the checkout item list using the current order.
     * Quantity changes are delegated to the controller.
     */
    public void setItems(
            Map<Product, Integer> items,
            BiConsumer<Product, Integer> onQuantityChange
    ) {
        Objects.requireNonNull(items, "Items cannot be null.");
        Objects.requireNonNull(onQuantityChange, "Quantity callback cannot be null.");

        itemList.removeAll();

        items.forEach((product, quantity) -> {
            if (itemList.getComponentCount() > 0) {
                itemList.add(Box.createVerticalStrut(8));
            }

            itemList.add(
                    new CheckoutItemRow(
                            product,
                            quantity,
                            newQty -> onQuantityChange.accept(product, newQty)
                    )
            );
        });

        itemCount = items.size();

        centerLayout.show(
                center,
                items.isEmpty() ? EMPTY : ITEMS
        );

        showEmptyState(items.isEmpty());

        itemList.revalidate();
        itemList.repaint();
    }

    /**
     * Updates the displayed subtotal, discount, and total.
     */
    public void setSummary(BigDecimal subtotal, BigDecimal discount, BigDecimal total) {
        this.total = Objects.requireNonNull(total, "Total cannot be null.");

        subtotalLabel.setText(Money.format(subtotal));
        discountLabel.setText(Money.format(discount));
        totalLabel.setText(Money.format(total));

        // Forget the previous payment when the order becomes empty.
        if (total.signum() == 0) {
            receivedField.setText("");
        }

        updatePayment();
    }

    /**
     * Enables or disables payment controls while saving an order.
     */
    public void setBusy(boolean busy) {
        this.busy = busy;

        placeOrderButton.setText(busy ? "Saving…" : "Place Order");

        for (Component component : quickCash.getComponents()) {
            component.setEnabled(!busy);
        }

        receivedField.setEnabled(!busy);
        clearButton.setEnabled(!busy && itemCount > 0);

        updatePayment();
    }

    /**
     * Returns the parsed amount received from the customer.
     */
    public BigDecimal getAmountReceived() {
        return parseAmount(receivedField.getText());
    }

    // ---- order actions ----

    /**
     * A single item is cleared immediately.
     * Orders containing two or more items require confirmation.
     */
    private void confirmClear(Runnable onClear) {
        if (busy || itemCount == 0) {
            return;
        }

        if (itemCount >= 2) {
            Object[] options = {"Clear order", "Keep order"};

            int choice = JOptionPane.showOptionDialog(
                    this,
                    "Remove all " + itemCount + " items from this order?",
                    "Clear order",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    options,
                    options[1]
            );

            // Keep order is the default choice.
            if (choice != 0) {
                return;
            }
        }

        onClear.run();
    }

    /**
     * Hides the summary and payment section when the order is empty.
     */
    private void showEmptyState(boolean empty) {
        footer.setVisible(!empty);
        clearButton.setEnabled(!empty && !busy);

        revalidate();
        repaint();
    }

    // ---- component building ----

    /**
     * Builds the checkout header.
     */
    private JPanel buildTop() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(
                BorderFactory.createEmptyBorder(14, 16, 10, 16)
        );

        JLabel title = new JLabel("Checkout");
        title.setFont(Theme.bold(title, 18f));

        top.add(title, BorderLayout.WEST);
        top.add(clearButton, BorderLayout.EAST);

        return top;
    }

    /**
     * Builds the order item area and its empty state.
     */
    private JPanel buildCenter() {
        center.setOpaque(false);

        JLabel empty = new JLabel(
                "Add item to checkout",
                SwingConstants.CENTER
        );

        empty.setForeground(Theme.MUTED);

        itemList.setOpaque(false);
        itemList.setLayout(
                new BoxLayout(itemList, BoxLayout.Y_AXIS)
        );

        // Wrapper keeps checkout rows at their natural height.
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(itemList, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        scroll.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );

        scroll.getVerticalScrollBar().setUnitIncrement(16);

        center.add(empty, EMPTY);
        center.add(scroll, ITEMS);

        RoundedPanel area = new RoundedPanel(
                Theme.APP_BG,
                null,
                12
        );

        area.setLayout(new BorderLayout());
        area.setBorder(
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        );

        area.add(center, BorderLayout.CENTER);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        outer.setBorder(
                BorderFactory.createEmptyBorder(0, 16, 12, 16)
        );

        outer.add(area, BorderLayout.CENTER);

        return outer;
    }

    /**
     * Builds the summary, payment input, quick-cash buttons,
     * change display, and place-order button.
     */
    private JPanel buildFooter() {
        footer = new JPanel(new GridBagLayout());
        footer.setOpaque(false);

        footer.setBorder(
                BorderFactory.createEmptyBorder(0, 16, 16, 16)
        );

        // Subtotal, discount, and total.
        stack(footer, summaryRow("Subtotal", subtotalLabel, false), 0);
        stack(footer, summaryRow("Discount", discountLabel, false), 2);
        stack(footer, new JSeparator(), 6);
        stack(footer, summaryRow("Total", totalLabel, true), 6);

        // Amount received.
        stack(footer, new JLabel("Amount Received"), 14);

        receivedField.putClientProperty(
                "JTextField.placeholderText",
                "Enter amount"
        );

        receivedField.putClientProperty(
                "JTextField.showClearButton",
                true
        );

        receivedField.setPreferredSize(
                new Dimension(0, 38)
        );

        // Recalculate change whenever the payment input changes.
        receivedField.getDocument().addDocumentListener(
                new DocumentListener() {

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        updatePayment();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        updatePayment();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        updatePayment();
                    }
                }
        );

        stack(footer, receivedField, 6);

        // Quick-cash buttons.
        quickCash = buildQuickCash();
        stack(footer, quickCash, 6);

        // Change display.
        changeTitle.setFont(
                Theme.bold(changeTitle, 13f)
        );

        changeAmount.setFont(
                Theme.bold(changeAmount, 18f)
        );

        changeBox.setLayout(
                new BorderLayout(0, 2)
        );

        changeBox.setBorder(
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        );

        changeBox.add(changeTitle, BorderLayout.NORTH);
        changeBox.add(changeAmount, BorderLayout.CENTER);

        stack(footer, changeBox, 10);

        // Place-order action.
        placeOrderButton.setPreferredSize(
                new Dimension(0, 42)
        );

        stack(footer, placeOrderButton, 14);

        return footer;
    }

    /**
     * Creates the quick-cash buttons.
     * Exact uses the current order total.
     */
    private JPanel buildQuickCash() {
        JPanel row = new JPanel(
                new GridLayout(1, 5, 6, 0)
        );

        row.setOpaque(false);

        row.add(
                quickCashButton("Exact", () -> total)
        );

        for (int bill : new int[]{100, 200, 500, 1000}) {
            BigDecimal amount = BigDecimal.valueOf(bill);

            row.add(
                    quickCashButton(
                            Money.format(amount),
                            () -> amount
                    )
            );
        }

        return row;
    }

    /**
     * Creates an individual quick-cash button.
     */
    private AppButton quickCashButton(String label, Supplier<BigDecimal> amount) {
        AppButton button = new AppButton(
                label,
                e -> receivedField.setText(
                        amount.get()
                                .stripTrailingZeros()
                                .toPlainString()
                )
        );

        button.setFocusable(false);
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.setFont(Theme.regular(button, 12f));
        button.setMargin(new Insets(2, 4, 2, 4));
        button.setPreferredSize(new Dimension(0, 32));

        return button;
    }

    /**
     * Builds a summary row with a label and its corresponding value.
     */
    private JPanel summaryRow(String text, JLabel value, boolean emphasized) {
        JLabel label = new JLabel(text);

        label.setForeground(
                emphasized ? Theme.NAVY : Theme.MUTED
        );

        if (emphasized) {
            label.setFont(Theme.bold(label, 16f));
        }

        value.setFont(Theme.bold(value, emphasized ? 20f : 14f));
        value.setForeground(Theme.NAVY);

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        row.add(label, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);

        return row;
    }

    /**
     * Adds a component as a full-width GridBagLayout row.
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

    // ---- payment calculations ----

    /**
     * Updates the displayed change or remaining payment.
     * The place-order button is enabled only when:
     * - The order total is greater than zero.
     * - The received amount covers the total.
     * - An order is not currently being saved.
     */
    private void updatePayment() {
        BigDecimal difference = getAmountReceived().subtract(total);

        boolean enough = difference.signum() >= 0;
        boolean typed = !receivedField.getText().isBlank();

        if (!typed) {
            // Nothing entered: show a neutral state.
            changeTitle.setText("Change");
            changeAmount.setText("—");
            changeAmount.setForeground(Theme.MUTED);
            changeBox.setFill(Theme.APP_BG);

        } else {
            changeTitle.setText(enough ? "Change" : "Short by");

            changeAmount.setText(Money.format(difference.abs()));

            changeAmount.setForeground(enough ? Theme.SUCCESS : Theme.DANGER);

            changeBox.setFill(enough ? Theme.SUCCESS_BG : Theme.DANGER_BG);
        }

        placeOrderButton.setEnabled(!busy && total.signum() > 0 && enough);
    }

    /**
     * Parses a customer payment amount.
     * Accepted examples:
     * 500
     * 1,250
     * 1,250.50
     * ₱500
     * ₱ 1,250.50
     * Invalid input returns BigDecimal.ZERO.
     */
    private static BigDecimal parseAmount(String text) {
        if (text == null || text.isBlank()) {
            return BigDecimal.ZERO;
        }

        String cleaned = text.trim()
                .replaceFirst("^₱\\s*", "")
                .replaceAll("\\s+", "");

        // Accept correctly grouped commas and up to two decimal places.
        String pattern =
                "(?:\\d+|\\d{1,3}(?:,\\d{3})+)(?:\\.\\d{1,2})?";

        if (!cleaned.matches(pattern)) {
            return BigDecimal.ZERO;
        }

        try {
            return new BigDecimal(cleaned.replace(",", ""));
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
