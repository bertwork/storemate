package util;

import javax.swing.*;
import java.sql.SQLException;

/**
 * Handles errors that prevent StoreMate from starting.
 */
public final class StartupErrorHandler {

    private StartupErrorHandler() {}

    /**
     * Displays an appropriate startup error message.
     */
    public static void show(Throwable cause) {
        if (cause == null) {
            cause = new IllegalStateException("An unknown error occurred during startup.");
        }

        // Keep the full exception details available for debugging.
        cause.printStackTrace();

        String message;

        if (cause instanceof SQLException) {
            message = "Could not load products from the database.\n\n" + cause.getMessage() + "\n\nIs Laragon running (MySQL started)?";

        } else {
            message = "StoreMate could not start.\n\n" + cause;
        }

        JOptionPane.showMessageDialog(
                null,
                message,
                "StoreMate",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
