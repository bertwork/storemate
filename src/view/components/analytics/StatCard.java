package view.components.analytics;

import util.Theme;
import view.components.RoundedPanel;

import javax.swing.*;
import java.awt.*;

/** A summary number with a small title above it (e.g. "Revenue"). */
public class StatCard extends RoundedPanel {

    private final JLabel valueLabel = new JLabel("—");

    public StatCard(String title) {
        super(Theme.SURFACE, Theme.BORDER, 14);
        setLayout(new BorderLayout(0, 4));
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.regular(titleLabel, 12f));
        titleLabel.setForeground(Theme.MUTED);

        valueLabel.setFont(Theme.bold(valueLabel, 24f));
        valueLabel.setForeground(Theme.NAVY);

        add(titleLabel, BorderLayout.NORTH);
        add(valueLabel, BorderLayout.CENTER);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }
}