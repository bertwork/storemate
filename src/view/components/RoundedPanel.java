package view.components;

import javax.swing.*;
import java.awt.*;

public class RoundedPanel extends JPanel {

    private Color fill;
    private Color border;
    private final int arc;

    public RoundedPanel(Color fill, Color border, int arc) {
        this.fill = fill;
        this.border = border;
        this.arc = arc;
        setOpaque(false); // we paint the background ourselves
    }

    public void setFill(Color fill) {
        this.fill = fill;
        repaint();
    }

    public void setBorderColor(Color border) {
        this.border = border;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        if (border != null) {
            g2.setColor(border);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        }
        g2.dispose();
    }
}