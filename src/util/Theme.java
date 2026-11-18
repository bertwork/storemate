package util;

import java.awt.*;

public class Theme {

    public static final Color NAVY         = new Color(0x112241);
    public static final Color ACCENT       = new Color(0x2563EB);
    public static final Color MUTED        = new Color(0x6B7280);
    public static final Color BORDER       = new Color(0xE5E7EB);
    public static final Color SUCCESS      = new Color(0x2E9E3E);
    public static final Color IMAGE_FILL   = new Color(0x7A9CF0);
    public static final Color IMAGE_FADED  = new Color(0xC9D6F7);

    public static final Color SURFACE      = Color.WHITE;
    public static final Color APP_BG       = new Color(0xF4F6FA);
    public static final Color FOOTER_BG    = new Color(0x14213D);
    public static final Color CARD_ADDED   = new Color(0xF7F7F8);

    public static final Color SUCCESS_BG   = new Color(0xD6EFDA);
    public static final Color PLACEHOLDER  = new Color(0xC4CAD4); // empty-state icons
    public static final Color DANGER       = new Color(0xEF4444);
    public static final Color DANGER_BG    = new Color(0xFEE2E2);

    /** Same size as the component's current font, bold. */
    public static Font bold(Component c) {
        return c.getFont().deriveFont(Font.BOLD);
    }

    public static Font bold(Component c, float size) {
        return c.getFont().deriveFont(Font.BOLD, size);
    }

    public static Font regular(Component c, float size) {
        return c.getFont().deriveFont(Font.PLAIN, size);
    }
}