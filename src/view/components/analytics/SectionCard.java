package view.components.analytics;

import util.Theme;
import view.components.RoundedPanel;

import javax.swing.*;
import java.awt.*;

/** White rounded card with a title, wrapping a chart or table. */
public class SectionCard extends RoundedPanel {

    public SectionCard(String title, JComponent content) {
        super(Theme.SURFACE, Theme.BORDER, 14);
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.bold(titleLabel, 14f));
        titleLabel.setForeground(Theme.NAVY);

        add(titleLabel, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
    }
}