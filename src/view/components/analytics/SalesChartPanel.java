package view.components.analytics;

import model.AnalyticsReport.Point;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import util.Theme;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;

/**
 * Displays sales performance over time using a bar chart.
 * This component owns its dataset and chart configuration.
 * It does not retrieve analytics data or communicate with the controller.
 */
public class SalesChartPanel extends JPanel {

    private final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

    private final JFreeChart chart = buildChart();

    public SalesChartPanel() {
        setLayout(new BorderLayout());

        // Keep the chart inside a Swing ChartPanel.
        add(createChartPanel(), BorderLayout.CENTER);
    }

    /**
     * Updates the chart using the supplied sales points.
     * @param sales sales data grouped by date or period
     */
    public void setSales(List<Point> sales) {
        Objects.requireNonNull(sales, "Sales list cannot be null");

        dataset.clear();

        // Avoid displaying zero-value bars when the selected range has no sales.
        boolean anySales = sales.stream()
                .anyMatch(point -> point.amount().signum() > 0);

        if (!anySales) {
            return;
        }

        for (Point point : sales) {
            dataset.addValue(point.amount(), "Sales", point.label());
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
     * Builds and styles the sales bar chart.
     */
    private JFreeChart buildChart() {
        JFreeChart chart = ChartFactory.createBarChart(
                null,
                null,
                null,
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );

        chart.setBackgroundPaint(Theme.SURFACE);
        chart.setBorderVisible(false);
        chart.setAntiAlias(true);

        CategoryPlot plot = chart.getCategoryPlot();

        plot.setBackgroundPaint(Theme.SURFACE);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(Theme.BORDER);
        plot.setDomainGridlinesVisible(false);

        // Message displayed when the dataset contains no sales.
        plot.setNoDataMessage("No sales in this range");
        plot.setNoDataMessageFont(font(12f));
        plot.setNoDataMessagePaint(Theme.MUTED);

        // Configure horizontal category labels.
        CategoryAxis domainAxis = plot.getDomainAxis();

        domainAxis.setCategoryLabelPositions(
                CategoryLabelPositions.UP_45
        );
        domainAxis.setAxisLineVisible(false);
        domainAxis.setTickMarksVisible(false);
        domainAxis.setTickLabelFont(font(11f));
        domainAxis.setTickLabelPaint(Theme.MUTED);

        // Configure the revenue axis.
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();

        rangeAxis.setAxisLineVisible(false);
        rangeAxis.setTickMarksVisible(false);
        rangeAxis.setTickLabelFont(font(11f));
        rangeAxis.setTickLabelPaint(Theme.MUTED);
        rangeAxis.setNumberFormatOverride(
                new DecimalFormat("₱#,##0")
        );

        // Configure the bar renderer.
        BarRenderer renderer = (BarRenderer) plot.getRenderer();

        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setSeriesPaint(0, Theme.ACCENT);
        renderer.setMaximumBarWidth(0.08);

        renderer.setDefaultToolTipGenerator(
                new StandardCategoryToolTipGenerator(
                        "{1}: ₱{2}",
                        new DecimalFormat("#,##0.##")
                )
        );

        return chart;
    }

    /**
     * Uses the application's default font family.
     */
    private static Font font(float size) {
        return UIManager.getFont("Label.font").deriveFont(size);
    }
}
