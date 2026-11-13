package app;

import model.Product;
import model.repository.ProductRepository;
import view.SplashWindow;

import javax.swing.*;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * Handles the initial loading process before StoreMate opens.
 * Loads products in the background so the Swing Event Dispatch Thread
 * remains responsive while the splash screen is visible.
 */
public final class StartupLoader {

    private static final long MIN_SPLASH_MS = 1500;

    private StartupLoader() {}

    /**
     * Starts the splash screen and loads the initial product data.
     * @param onSuccess called with the loaded products when startup succeeds
     * @param onFailure called when startup loading fails
     */
    public static void start(
            Consumer<List<Product>> onSuccess,
            Consumer<Throwable> onFailure
    ) {

        Objects.requireNonNull(onSuccess, "Success callback must not be null.");
        Objects.requireNonNull(onFailure, "Failure callback must not be null.");

        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException(
                    "StartupLoader.start() must be called on the EDT."
            );
        }

        SplashWindow splash = new SplashWindow();
        splash.setVisible(true);

        new SwingWorker<List<Product>, String>() {

            @Override
            protected List<Product> doInBackground() throws Exception {
                long started = System.currentTimeMillis();

                // Load products from the database.
                publish("Loading products…");

                List<Product> products = new ProductRepository().findAll();

                // Keep the splash screen visible briefly for a smoother startup.
                publish("Starting StoreMate…");

                long elapsed = System.currentTimeMillis() - started;
                long remaining = MIN_SPLASH_MS - elapsed;

                if (remaining > 0) {
                    Thread.sleep(remaining);
                }

                return products;
            }

            @Override
            protected void process(List<String> messages) {
                // Display only the most recent loading message.
                if (!messages.isEmpty()) {
                    splash.setStatus(messages.getLast());
                }
            }

            @Override
            protected void done() {
                try {
                    // get() retrieves the background result on the EDT.
                    List<Product> products = get();

                    // Close the splash before opening the main window.
                    splash.dispose();

                    // Continue with application composition.
                    onSuccess.accept(products);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    splash.dispose();
                    onFailure.accept(e);

                } catch (ExecutionException e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    splash.dispose();
                    onFailure.accept(cause);

                } catch (CancellationException e) {
                    splash.dispose();
                    onFailure.accept(e);

                } catch (RuntimeException e) {
                    // Includes cancellation and errors during window creation.
                    splash.dispose();
                    onFailure.accept(e);
                }
            }

        }.execute();
    }
}
