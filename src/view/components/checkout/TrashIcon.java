package view.components.checkout;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

/**
 * Custom trash icon used by checkout item controls.
 */
public class TrashIcon implements Icon {

    private final int size;
    private final Color color;

    public TrashIcon(int size, Color color) {
        this.size = size;
        this.color = color;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );
        g2.setRenderingHint(
                RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE
        );

        g2.translate(x, y);
        g2.scale(size / 16.0, size / 16.0);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(
                1.4f,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
        ));

        // Draw the lid and handle.
        g2.draw(new Line2D.Float(2, 4, 14, 4));

        Path2D handle = new Path2D.Float();
        handle.moveTo(6, 4);
        handle.lineTo(6, 2);
        handle.lineTo(10, 2);
        handle.lineTo(10, 4);
        g2.draw(handle);

        // Draw the container and inner lines.
        Path2D body = new Path2D.Float();
        body.moveTo(3.5, 4);
        body.lineTo(4.5, 14);
        body.lineTo(11.5, 14);
        body.lineTo(12.5, 4);
        g2.draw(body);

        g2.draw(new Line2D.Float(6.5f, 7, 6.5f, 11));
        g2.draw(new Line2D.Float(9.5f, 7, 9.5f, 11));

        g2.dispose();
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }
}