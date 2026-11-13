package view;

/**
 * Defines the available application screens and their display labels.
 */
public enum Screen {

    ORDER("Order"),
    ANALYTICS("Analytics"),
    PRODUCT("Test Panel");

    private final String label;

    /**
     * Creates a screen with its display label.
     */
    Screen(String label) {
        this.label = label;
    }

    /**
     * Returns the display label used by UI components.
     */
    @Override
    public String toString() {
        return label;
    }
}
