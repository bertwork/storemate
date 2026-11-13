package controller;

import model.AnalyticsReport;
import model.repository.AnalyticsRepository;
import view.panels.AnalyticsPanel;

import javax.swing.*;
import java.time.LocalDate;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controls the Analytics screen and loads reports from the repository.
 * The controller listens for refresh requests from AnalyticsPanel,
 * retrieves the requested report, and sends the result back to the view.
 * Database queries run asynchronously using SwingWorker so that
 * the Swing Event Dispatch Thread remains responsive.
 */
public class AnalyticsController implements AnalyticsPanel.Listener {

    private static final Logger LOGGER = Logger.getLogger(AnalyticsController.class.getName());

    // ---- dependencies ----

    private final AnalyticsPanel view;
    private final AnalyticsRepository repository;

    /*
     * Identifies the most recent report request.
     * If the user changes the date range while an earlier request
     * is still running, the earlier result will be ignored.
     */
    private long latestRequest = 0;

    public AnalyticsController(AnalyticsPanel view, AnalyticsRepository repository) {
        this.view = Objects.requireNonNull(view, "AnalyticsPanel cannot be null.");
        this.repository = Objects.requireNonNull(repository, "AnalyticsRepository cannot be null.");

        // Register this controller as the panel's event listener.
        view.setListener(this);
    }

    // ---- analytics actions ----

    /**
     * Loads analytics for the requested date range.
     * Every refresh creates a new request identifier.
     * Only the latest request is allowed to update the view.
     */
    @Override
    public void onRefresh(LocalDate from, LocalDate to) {
        Objects.requireNonNull(from, "Start date cannot be null.");
        Objects.requireNonNull(to, "End date cannot be null.");

        if (from.isAfter(to)) {
            view.showError("The start date cannot be after the end date.");
            return;
        }

        // Mark this as the newest analytics request.
        long request = ++latestRequest;

        // Show the loading state while the report is being retrieved.
        view.setLoading(true);

        /*
         * Execute the repository query in the background.
         * doInBackground() runs outside the EDT.
         * done() runs on the EDT, making it safe to update Swing components.
         */
        new SwingWorker<AnalyticsReport, Void>() {

            /**
             * Retrieves the analytics report for the selected date range.
             */
            @Override
            protected AnalyticsReport doInBackground() throws Exception {
                return repository.load(from, to);
            }

            /**
             * Processes the result after the background query finishes.
             */
            @Override
            protected void done() {

                /*
                 * Ignore outdated results.
                 * A newer request is already responsible for updating
                 * the loading state and displaying the latest report.
                 */
                if (request != latestRequest) {
                    return;
                }

                // The current request has finished.
                view.setLoading(false);

                try {
                    AnalyticsReport report = get();

                    // Display the retrieved report.
                    view.showReport(report);

                } catch (InterruptedException e) {
                    /*
                     * Restore the interrupt status instead of silently
                     * swallowing the interruption.
                     */
                    Thread.currentThread().interrupt();

                    LOGGER.log(Level.WARNING, "Interrupted while retrieving analytics.", e);
                    view.showError("The analytics report could not be retrieved " + "because the operation was interrupted.");

                } catch (ExecutionException e) {
                    /*
                     * The repository query failed.
                     * Safely retrieve the underlying exception instead
                     * of assuming that getCause() is always available.
                     */
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    LOGGER.log(Level.SEVERE, "Could not load analytics.", cause);
                    view.showError("Could not load analytics.\n\n" + getErrorMessage(cause));

                } catch (CancellationException e) {
                    // Handle an unexpectedly canceled report request.
                    LOGGER.log(Level.WARNING, "Analytics request was cancelled.", e);

                    view.showError("The analytics request was cancelled.");
                }
            }

        }.execute();
    }

    // ---- error handling ----

    /**
     * Returns a readable error message.
     * Some exceptions do not contain a message, so a fallback is
     * provided instead of displaying "null" in the error dialog.
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
