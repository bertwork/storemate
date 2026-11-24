package view.components.analytics;

import model.AnalyticsReport.CategorySales;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import util.Theme;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;

/**
 * Displays sales distribution across product categories using a pie chart.
 * This component owns its dataset, category color palette, and chart styling.
 * It does not retrieve analytics data or communicate with the controller.
 */
public class CategoryChartPanel extends JPanel {

    // Colors assigned to categorize in the order they are received.
    private static final Color[] PALETTE = {
            Theme.ACCENT,
            Theme.IMAGE_FILL,
            Theme.FOOTER_BG,
            Theme.SUCCESS,
            new Color(0xF59E0B),
            new Color(0xEF4444)
    };

    // Use String as the category key to avoid raw-type warnings.
    private final DefaultPieDataset<String> dataset = new DefaultPieDataset<>();

    // Keep a strongly typed reference to the pie plot.
    private final PiePlot<String> plot = new PiePlot<>(dataset);

    private final JFreeChart chart = buildChart();

    public CategoryChartPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        add(createChartPanel(), BorderLayout.CENTER);
    }

    /**
     * Updates the pie chart using the supplied category sales.
     *
     * @param categories sales grouped by product category
     */
    public void setCategories(List<CategorySales> categories) {
        Objects.requireNonNull(categories, "Categories cannot be null");

        dataset.clear();

        int colorIndex = 0;

        for (CategorySales category : categories) {
            String name = category.category();
            dataset.setValue(name, category.amount());

            // Cycle through the palette when there are more categories than colors.
            plot.setSectionPaint(name, PALETTE[colorIndex++ % PALETTE.length]);
        }
    }

    /**
     * Creates the Swing wrapper for the JFreeChart.
     */
    private ChartPanel createChartPanel() {
        ChartPanel panel = new ChartPanel(chart);

        panel.setPopupMenu(null);
        panel.setMouseWheelEnabled(false);
        panel.setDomainZoomable(false);
        panel.setRangeZoomable(false);

        panel.setBackground(Theme.SURFACE);
        panel.setMinimumDrawWidth(0);
        panel.setMinimumDrawHeight(0);
        panel.setMaximumDrawWidth(Integer.MAX_VALUE);
        panel.setMaximumDrawHeight(Integer.MAX_VALUE);
        panel.setPreferredSize(new Dimension(100, 100));

        return panel;
    }

    /**
     * Builds and styles the category pie chart.
     */
    private JFreeChart buildChart() {
        // Construct the chart directly to retain the typed PiePlot<String>.
        JFreeChart chart = new JFreeChart(
                null,
                JFreeChart.DEFAULT_TITLE_FONT,
                plot,
                false
        );

        chart.setBackgroundPaint(Theme.SURFACE);
        chart.setBorderVisible(false);
        chart.setAntiAlias(true);

        plot.setBackgroundPaint(Theme.SURFACE);
        plot.setOutlineVisible(false);
        plot.setShadowPaint(null);
        plot.setSectionOutlinesVisible(false);

        // Keep category labels outside the pie with the original format.
        plot.setLabelFont(font());
        plot.setLabelPaint(Theme.NAVY);
        plot.setLabelBackgroundPaint(null);
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        plot.setLabelGenerator(
                new StandardPieSectionLabelGenerator("{0} {2}")
        );

        // Give labels and their connecting lines more room.
        plot.setLabelGap(0.04);
        plot.setMaximumLabelWidth(0.25);
        plot.setLabelLinkMargin(0.025);
        plot.setLabelLinkPaint(Theme.MUTED);
        plot.setLabelLinkStroke(new BasicStroke(0.8f));

        // Configure hover tooltips.
        plot.setToolTipGenerator(
                new StandardPieToolTipGenerator(
                        "{0}: ₱{1} ({2})",
                        new DecimalFormat("#,##0.##"),
                        new DecimalFormat("0.0%")
                )
        );

        // Message displayed when the dataset contains no sales.
        plot.setNoDataMessage("No sales in this range");
        plot.setNoDataMessageFont(font());
        plot.setNoDataMessagePaint(Theme.MUTED);

        return chart;
    }

    /**
     * Uses the application's default font family.
     */
    private static Font font() {
        return UIManager.getFont("Label.font").deriveFont(12f);
    }
}
