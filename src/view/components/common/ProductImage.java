package view.components.common;

import util.Theme;
import view.components.RoundedPanel;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * Rounded image container with a placeholder background.
 * Supports cover scaling, image cropping, and a faded state.
 */
public class ProductImage extends RoundedPanel {

    private final int arc;
    private final boolean cover;

    private BufferedImage image;
    private boolean faded = false;

    public ProductImage(int arc, boolean cover) {
        super(Theme.IMAGE_FILL, null, arc);
        this.arc = arc;
        this.cover = cover;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        repaint();
    }

    // Applies a faded appearance to products already added to the order.
    public void setFaded(boolean faded) {
        this.faded = faded;
        setFill(faded ? Theme.IMAGE_FADED : Theme.IMAGE_FILL);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (image == null) {
            return;
        }

        int w = getWidth() - 1;
        int h = getHeight() - 1;

        if (w <= 0 || h <= 0) {
            return;
        }

        // Calculate image scaling based on the selected display mode.
        double scaleX = (double) w / image.getWidth();
        double scaleY = (double) h / image.getHeight();

        double scale = cover ? Math.max(scaleX, scaleY) : Math.min(scaleX, scaleY);

        int drawW = (int) Math.round(image.getWidth() * scale);
        int drawH = (int) Math.round(image.getHeight() * scale);

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );
        g2.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC
        );
        g2.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
        );

        // Clip the image to the rounded container boundaries.
        g2.clip(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));

        if (faded) {
            g2.setComposite(AlphaComposite.SrcOver.derive(0.4f));
        }

        // Center the scaled image inside the container.
        g2.drawImage(
                image,
                (w - drawW) / 2,
                (h - drawH) / 2,
                drawW,
                drawH,
                null
        );

        g2.dispose();
    }
}
