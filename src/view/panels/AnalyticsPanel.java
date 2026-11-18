package view.panels;

import model.AnalyticsReport;
import util.Theme;
import view.components.analytics.AnalyticsSummaryPanel;
import view.components.analytics.CategoryChartPanel;
import view.components.analytics.SalesChartPanel;
import view.components.analytics.SectionCard;
import view.components.analytics.TopProductsPanel;
import view.components.common.AppButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

/**
 * Main Analytics screen.
 * Responsibilities:
 * - Managing the selected date range.
 * - Providing date-range shortcuts.
 * - Requesting analytics updates from the controller.
 * - Coordinating the analytics presentation components.
 * Individual components handle their own data presentation and styling.
 */
public class AnalyticsPanel extends JPanel {

    /**
     * Defines the communication contract with the analytics controller.
     */
    public interface Listener {
        void onRefresh(LocalDate from, LocalDate to);
    }

    // Date range controls.
    private final JSpinner fromSpinner = dateSpinner();
    private final JSpinner toSpinner = dateSpinner();

    // Prevents recursive change events while adjusting the date range.
    private boolean adjusting = false;

    // Analytics presentation components.
    private final AnalyticsSummaryPanel summaryPanel = new AnalyticsSummaryPanel();

    private final SalesChartPanel salesChartPanel = new SalesChartPanel();

    private final CategoryChartPanel categoryChartPanel = new CategoryChartPanel();

    private final TopProductsPanel topProductsPanel = new TopProductsPanel();

    // Controller communication.
    private Listener listener;

    public AnalyticsPanel() {
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Initialize the default date range to the last 30 days.
        setRange(
                LocalDate.now().minusDays(29),
                LocalDate.now(),
                false
        );

        add(buildToolbar(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        // Request updated analytics whenever the screen is opened.
        fromSpinner.addChangeListener(e -> rangeChanged(true));
        toSpinner.addChangeListener(e -> rangeChanged(false));

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                fireRefresh();
            }
        });
    }

    /**
     * Registers the controller responsible for retrieving analytics data.
     */
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    /**
     * Distributes the retrieved report to the corresponding presentation
     * components.
     */
    public void showReport(AnalyticsReport report) {
        summaryPanel.setSummary(report.summary());
        salesChartPanel.setSales(report.salesOverTime());
        categoryChartPanel.setCategories(report.salesByCategory());
        topProductsPanel.setProducts(report.topProducts());
    }

    /**
     * Updates the cursor to indicate whether analytics are being loaded.
     */
    public void setLoading(boolean loading) {
        setCursor(Cursor.getPredefinedCursor(
                loading ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR
        ));
    }

    /**
     * Displays an error message when analytics cannot be retrieved.
     */
    public void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "StoreMate",
                JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Creates a date spinner using the application's date display format.
     */
    private static JSpinner dateSpinner() {
        JSpinner spinner = new JSpinner(
                new SpinnerDateModel(
                        new Date(),
                        null,
                        null,
                        Calendar.DAY_OF_MONTH
                )
        );

        spinner.setEditor(new JSpinner.DateEditor(spinner, "MMM d, yyyy"));
        spinner.setPreferredSize(new Dimension(140, 34));

        return spinner;
    }

    /**
     * Returns the selected starting date.
     */
    private LocalDate getFrom() {
        return toLocalDate(fromSpinner);
    }

    /**
     * Returns the selected ending date.
     */
    private LocalDate getTo() {
        return toLocalDate(toSpinner);
    }

    /**
     * Converts a spinner's Date value into LocalDate.
     */
    private static LocalDate toLocalDate(JSpinner spinner) {
        return ((Date) spinner.getValue())
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    /**
     * Updates a date spinner using a LocalDate value.
     */
    private static void setDate(JSpinner spinner, LocalDate date) {
        spinner.setValue(
                Date.from(
                        date.atStartOfDay(ZoneId.systemDefault()).toInstant()
                )
        );
    }

    /**
     * Keeps the selected date range valid.
     * If one date moves past the other, the opposite date automatically
     * follows it.
     * @param fromMoved whether the starting date was changed
     */
    private void rangeChanged(boolean fromMoved) {
        if (adjusting) {
            return;
        }

        adjusting = true;

        if (getFrom().isAfter(getTo())) {
            if (fromMoved) {
                setDate(toSpinner, getFrom());
            } else {
                setDate(fromSpinner, getTo());
            }
        }

        adjusting = false;

        fireRefresh();
    }

    /**
     * Updates both date controls.
     * @param from beginning of the selected range
     * @param to ending of the selected range
     * @param refresh whether to request updated analytics
     */
    private void setRange(LocalDate from, LocalDate to, boolean refresh) {
        adjusting = true;

        setDate(fromSpinner, from);
        setDate(toSpinner, to);

        adjusting = false;

        if (refresh) {
            fireRefresh();
        }
    }

    /**
     * Notifies the controller that analytics should be refreshed.
     */
    private void fireRefresh() {
        if (listener != null) {
            listener.onRefresh(getFrom(), getTo());
        }
    }

    /**
     * Builds the toolbar containing the screen title, date controls,
     * and date-range shortcuts.
     */
    private JPanel buildToolbar() {
        JLabel title = new JLabel("Analytics");
        title.setFont(Theme.bold(title, 20f));
        title.setForeground(Theme.NAVY);

        JPanel controls = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0)
        );

        controls.setOpaque(false);

        controls.add(new JLabel("From"));
        controls.add(fromSpinner);

        controls.add(new JLabel("To"));
        controls.add(toSpinner);

        controls.add(Box.createHorizontalStrut(8));

        controls.add(new AppButton(
                "Today",
                e -> setRange(
                        LocalDate.now(),
                        LocalDate.now(),
                        true
                )
        ));

        controls.add(new AppButton(
                "Last 7 days",
                e -> setRange(
                        LocalDate.now().minusDays(6),
                        LocalDate.now(),
                        true
                )
        ));

        controls.add(new AppButton(
                "Last 30 days",
                e -> setRange(
                        LocalDate.now().minusDays(29),
                        LocalDate.now(),
                        true
                )
        ));

        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);

        bar.add(title, BorderLayout.WEST);
        bar.add(controls, BorderLayout.EAST);

        return bar;
    }

    /**
     * Assembles the analytics presentation components into the screen layout.
     */
    private JPanel buildBody() {

        // Summary statistics.
        JPanel cards = new JPanel(new BorderLayout());
        cards.setOpaque(false);
        cards.add(summaryPanel, BorderLayout.CENTER);

        // Charts arranged side by side.
        JPanel charts = new JPanel(new GridBagLayout());
        charts.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();

        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1;

        // Allocate more space to the sales-over-time chart.
        gc.gridx = 0;
        gc.weightx = 0.62;
        gc.insets = new Insets(0, 0, 0, 6);

        charts.add(new SectionCard("Sales over time", salesChartPanel), gc);

        // Display the category distribution chart beside it.
        gc.gridx = 1;
        gc.weightx = 0.38;
        gc.insets = new Insets(0, 6, 0, 0);

        charts.add(new SectionCard("Sales by category", categoryChartPanel), gc);

        // Combine the charts and top-selling products section.
        JPanel lower = new JPanel(new GridLayout(2, 1, 0, 12));

        lower.setOpaque(false);
        lower.add(charts);
        lower.add(new SectionCard("Top selling products", topProductsPanel));

        // Main content layout.
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);

        body.add(cards, BorderLayout.NORTH);
        body.add(lower, BorderLayout.CENTER);

        return body;
    }
}
