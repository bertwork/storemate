package util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Finds the picture for a product: its own image_path, else its category's image, else null
 * (the caller then keeps the blue placeholder). Every file is read and scaled once, then reused.
 */
public class ImageLoader {

    private static final String CATEGORY_DIR = "/resources/images/categories/";
    private static final String PRODUCT_DIR  = "/resources/images/products/";
    private static final String[] EXTENSIONS = {"png", "jpg", "jpeg"};
    private static final int MAX_WIDTH = 480; // bigger files are shrunk once, so painting stays fast

    private static final Map<String, Optional<BufferedImage>> cache = new ConcurrentHashMap<>();

    /** imagePath may be null/blank. Returns null when nothing is found. */
    public static BufferedImage productImage(String imagePath, String category) {
        if (imagePath != null && !imagePath.isBlank()) {
            // "coke.png" -> /resources/images/products/coke.png ; "/some/path.png" is used as is
            String path = imagePath.startsWith("/") ? imagePath : PRODUCT_DIR + imagePath;
            BufferedImage own = load(path);
            if (own != null) return own;
        }
        return categoryImage(category);
    }

    /** "Personal Care" -> /resources/images/categories/personal_care.(png|jpg|jpeg) */
    public static BufferedImage categoryImage(String category) {
        if (category == null || category.isBlank()) return null;
        String name = category.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
        for (String ext : EXTENSIONS) {
            BufferedImage image = load(CATEGORY_DIR + name + "." + ext);
            if (image != null) return image;
        }
        return null;
    }

    private static BufferedImage load(String path) {
        return cache.computeIfAbsent(path, p -> Optional.ofNullable(read(p))).orElse(null);
    }

    private static BufferedImage read(String path) {
        URL url = ImageLoader.class.getResource(path);
        if (url == null) return null; // file doesn't exist, caller falls back
        try {
            BufferedImage image = ImageIO.read(url);
            return image == null ? null : shrink(image);
        } catch (IOException e) {
            System.err.println("Could not read image: " + path);
            return null;
        }
    }

    private static BufferedImage shrink(BufferedImage source) {
        if (source.getWidth() <= MAX_WIDTH) return source;

        int w = MAX_WIDTH;
        int h = Math.max(1, source.getHeight() * MAX_WIDTH / source.getWidth());
        BufferedImage scaled = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaled.createGraphics();
        g2.drawImage(source.getScaledInstance(w, h, Image.SCALE_SMOOTH), 0, 0, null);
        g2.dispose();
        return scaled;
    }
}