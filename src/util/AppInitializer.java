package util;

import com.formdev.flatlaf.FlatLightLaf;

/**
 * Initializes the global application configuration.
 * Responsible for font rendering, custom font loading,
 * and setting up the application's FlatLaf Look and Feel.
 */
public final class AppInitializer {

    private AppInitializer() {}

    /**
     * Configures system properties, loads application fonts,
     * and initializes FlatLaf.
     */
    public static void initialize() {
        // Enable font antialiasing for smoother text rendering.
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Load custom fonts before creating Swing components.
        FontLoader.load();

        // Register the application's custom FlatLaf properties.
        FlatLightLaf.registerCustomDefaultsSource("resources.style");

        // Apply FlatLaf as the application's Look and Feel.
        if (!FlatLightLaf.setup()) {
            throw new IllegalStateException(
                    "Could not initialize FlatLaf Look and Feel."
            );
        }
    }
}
