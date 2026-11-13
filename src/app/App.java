package app;

import controller.AnalyticsController;
import controller.ConnectionStatusController;
import controller.OrderController;

import model.Product;
import model.repository.AnalyticsRepository;
import model.repository.OrderRepository;

import util.AppInitializer;
import util.StartupErrorHandler;

import view.MainFrame;
import view.panels.AnalyticsPanel;
import view.panels.OrderPanel;
import view.panels.TestPanel;

import javax.swing.*;
import java.util.List;

/**
 * StoreMate application entry point.
 * Coordinates startup and connects the application's main components.
 * Detailed initialization tasks are delegated to dedicated classes.
 */
public class App {

    public static void main(String[] args) {

        // Initialize the application's global UI configuration.
        try {
            AppInitializer.initialize();
        } catch (RuntimeException e) {
            StartupErrorHandler.show(e);
            System.exit(1);
            return;
        }

        // Start the application on Swing's Event Dispatch Thread.
        SwingUtilities.invokeLater(App::start);
    }

    /**
     * Starts the application by loading the required data in the background.
     */
    private static void start() {

        StartupLoader.start(
                App::openMainWindow,
                cause -> {
                    StartupErrorHandler.show(cause);
                    System.exit(1);
                }
        );
    }

    /**
     * Creates and connects the main application components.
     * This is the application's composition point:
     * repositories provide data, controllers handle logic,
     * and panels display the interface.
     */
    private static void openMainWindow(List<Product> products) {
        // Order screen.
        OrderPanel orderPanel = new OrderPanel(products);
        new OrderController(orderPanel, new OrderRepository());

        // Analytics screen.
        AnalyticsPanel analyticsPanel = new AnalyticsPanel();
        new AnalyticsController(analyticsPanel, new AnalyticsRepository());

        // TestPanel
        TestPanel testPanel = new TestPanel();

        // Main application window.
        MainFrame frame = new MainFrame(orderPanel, analyticsPanel, testPanel);

        // Footer connection status.
        new ConnectionStatusController(frame.getFooter());

        frame.setVisible(true);
    }
}