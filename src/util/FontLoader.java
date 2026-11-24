package util;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;

public class FontLoader {

    private static final String[] FONT_FILES = {
            "/resources/fonts/Nunito-Regular.ttf",
            "/resources/fonts/Nunito-Bold.ttf"
    };

    public static void load() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

        for (String path : FONT_FILES) {
            try (InputStream in = FontLoader.class.getResourceAsStream(path)) {
                if (in == null) {
                    System.err.println("Font not found: " + path);
                    continue;
                }
                ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, in));
            } catch (Exception e) {
                System.err.println("Could not load font: " + path);
                e.printStackTrace();
            }
        }
    }
}