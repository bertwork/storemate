package controller;

import model.Order;
import model.Product;
import model.Receipt;
import model.repository.OrderRepository;
import view.panels.OrderPanel;

import javax.swing.*;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controls the order screen and manages the current order.
 * The view reports user actions through its Listener interface.
 * This controller handles order operations, communicates with the
 * repository, and tells the view when its displayed data must change.
 * Database operations are executed in the background using SwingWorker
 * to prevent the Swing interface from freezing.
 */
public class OrderController implements OrderPanel.Listener {

    private static final Logger LOGGER = Logger.getLogger(OrderController.class.getName());

    // ---- dependencies ----

    private final Order order = new Order();
    private final OrderPanel view;
    private final OrderRepository orders;

    // Prevents order modifications while a save operation is running.
    private boolean saving = false;

    public OrderController(OrderPanel view, OrderRepository orders) {
        this.view = Objects.requireNonNull(view, "OrderPanel cannot be null.");
        this.orders = Objects.requireNonNull(orders, "OrderRepository cannot be null.");

        // Register this controller as the view's event listener.
        view.setListener(this);

        // Initialize the screen with the current empty order.
        refresh();
    }

    // ---- order actions ----

    /**
     * Adds a product to the current order.
     */
    @Override
    public void onAddProduct(Product product) {
        if (saving || product == null) {
            return;
        }

        order.add(product);

        // Update the product card's added state.
        view.setAdded(product, true);

        refresh();
    }

    /**
     * Updates an item's quantity.
     * A quantity of zero or less removes the product from the order.
     */
    @Override
    public void onQuantityChange(Product product, int quantity) {
        if (saving || product == null) {
            return;
        }

        order.setQuantity(product, quantity);

        if (quantity <= 0) {
            // Restore the product card to its normal state.
            view.setAdded(product, false);

            refresh();
        } else {
            // The checkout row already updates its displayed quantity.
            showSummary();
        }
    }

    /**
     * Clears the current order.
     */
    @Override
    public void onClear() {
        if (saving) {
            return;
        }

        clearOrder();
    }

    /**
     * Validates and saves the current order.
     */
    @Override
    public void onPlaceOrder() {
        // Prevent duplicate saves and empty orders.
        if (saving || order.isEmpty()) {
            return;
        }

        BigDecimal received = view.getAmountReceived();

        // Do not proceed if the customer has insufficient payment.
        if (received.compareTo(order.total()) < 0) {
            return;
        }

        /*
         * Create a snapshot before saving.
         * This ensures the saved order remains consistent even if
         * the original order changes after the save operation.
         */
        Order snapshot = order.copy();

        saving = true;
        view.setBusy(true);

        /*
         * Perform the database operation in the background.
         * doInBackground() runs outside the Swing Event Dispatch Thread.
         * done() runs on the Swing Event Dispatch Thread.
         */
        new SwingWorker<Long, Void>() {

            /**
             * Saves the order and returns its generated database ID.
             */
            @Override
            protected Long doInBackground() throws Exception {
                return orders.save(snapshot, received);
            }

            /**
             * Handles the result after the background operation finishes.
             */
            @Override
            protected void done() {
                saving = false;
                view.setBusy(false);

                try {
                    long orderId = get();

                    /*
                     * Create the receipt from the saved order snapshot.
                     * The original order is cleared only after saving succeeds.
                     */
                    Receipt receipt = Receipt.of(orderId, snapshot, received);
                    clearOrder();
                    view.showReceipt(receipt);

                } catch (InterruptedException e) {
                    /*
                     * Restore the interrupt status instead of silently
                     * swallowing the interruption.
                     */
                    Thread.currentThread().interrupt();
                    LOGGER.log(Level.WARNING, "Interrupted while retrieving the saved order result.", e);
                    view.showError("The order result could not be retrieved because " + "the operation was interrupted.");

                } catch (ExecutionException e) {
                    /*
                     * The background database operation failed.
                     * Keep the original order so the cashier can review
                     * it and attempt the save again.
                     */
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    LOGGER.log(Level.SEVERE, "Could not save the order.", cause);
                    refresh();
                    view.showError("Could not save the order.\n\n" + getErrorMessage(cause));

                } catch (CancellationException e) {
                    /*
                     * Handle unexpected cancellation without losing
                     * the customer's current order.
                     */
                    LOGGER.log(Level.WARNING, "Order saving was cancelled.", e);
                    refresh();
                    view.showError("The order saving operation was cancelled.");
                }
            }

        }.execute();
    }

    // ---- order state management ----

    /**
     * Clears all items from the current order and resets the product cards.
     */
    private void clearOrder() {
        // Restore every product card to its normal state.
        order.items()
                .keySet()
                .forEach(product -> view.setAdded(product, false));

        order.clear();

        refresh();
    }

    /**
     * Refreshes the item list and order summary.
     */
    private void refresh() {
        view.setItems(order.items());

        showSummary();
    }

    /**
     * Updates the subtotal, discount, and total displayed by the view.
     */
    private void showSummary() {
        view.setSummary(order.subtotal(), order.discount(), order.total());
    }

    // ---- error handling ----

    /**
     * Extracts a useful error message from an exception.
     * Some exceptions do not provide a message, so a fallback is used
     * instead of displaying the word "null" to the user.
     */
    private static String getErrorMessage(Throwable error) {
        if (error == null) {
            return "An unexpected error occurred.";
        }

        String message = error.getMessage();

        if (message == null || message.isBlank()) {
            return "An unexpected database error occurred.";
        }

        return message;
    }
}
