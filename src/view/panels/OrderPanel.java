package view.panels;

import model.Product;
import model.Receipt;
import view.dialogs.ReceiptDialog;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Main order screen containing the product grid and checkout panel.
 * This class handles presentation only.
 * Order operations are delegated to the controller through Listener.
 */
public class OrderPanel extends JPanel {

    /**
     * Actions that can be performed by the user.
     * Implemented by OrderController.
     */
    public interface Listener {
        void onAddProduct(Product product);
        void onQuantityChange(Product product, int quantity);
        void onClear();
        void onPlaceOrder();
    }

    private final ProductsPanel productsPanel;
    private final CheckoutPanel checkoutPanel;

    private Listener listener;

    public OrderPanel(List<Product> products) {
        setLayout(new BorderLayout(12, 0));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        productsPanel = new ProductsPanel(
                products,
                product -> getListener().onAddProduct(product)
        );

        checkoutPanel = new CheckoutPanel(
                () -> getListener().onClear(),
                () -> getListener().onPlaceOrder()
        );

        add(productsPanel, BorderLayout.CENTER);
        add(checkoutPanel, BorderLayout.EAST);
    }

    /**
     * Returns the registered listener.
     * @throws IllegalStateException if the controller has not registered yet
     */
    private Listener getListener() {
        if (listener == null) {
            throw new IllegalStateException("OrderController has not registered its listener.");
        }

        return listener;
    }

    public void focusSearch() {
        productsPanel.focusSearch();
    }

    /**
     * Registers the controller responsible for handling user actions.
     */
    public void setListener(Listener listener) {
        this.listener = Objects.requireNonNull(listener, "Listener cannot be null.");
    }

    // Controller -> View updates

    public void setAdded(Product product, boolean added) {
        productsPanel.setAdded(product, added);
    }

    public void setItems(Map<Product, Integer> items) {
        checkoutPanel.setItems(
                items,
                (product, quantity) ->
                        getListener().onQuantityChange(product, quantity)
        );
    }

    public void setSummary(BigDecimal subtotal, BigDecimal discount, BigDecimal total) {
        checkoutPanel.setSummary(subtotal, discount, total);
    }

    public BigDecimal getAmountReceived() {
        return checkoutPanel.getAmountReceived();
    }

    public void setBusy(boolean busy) {
        checkoutPanel.setBusy(busy);
    }

    public void showReceipt(Receipt receipt) {
        ReceiptDialog.show(this, receipt);
    }

    public void showInfo(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "StoreMate",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "StoreMate",
                JOptionPane.ERROR_MESSAGE
        );
    }
}