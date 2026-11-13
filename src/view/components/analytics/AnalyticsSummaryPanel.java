package view.components.analytics;

import model.AnalyticsReport.Summary;
import util.Money;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * Displays the four summary statistics at the top of the Analytics screen.
 * This panel only handles the summary cards' layout and values.
 * It does not retrieve data or communicate with the controller.
 */
public class AnalyticsSummaryPanel extends JPanel {

    // Individual summary cards.
    private final StatCard revenueCard = new StatCard("Revenue");
    private final StatCard ordersCard = new StatCard("Orders");
    private final StatCard averageCard = new StatCard("Average order");
    private final StatCard itemsCard = new StatCard("Items sold");

    public AnalyticsSummaryPanel() {
        // Keep the same layout and spacing as the original AnalyticsPanel.
        setLayout(new GridLayout(1, 4, 12, 0));

        add(revenueCard);
        add(ordersCard);
        add(averageCard);
        add(itemsCard);
    }

    /**
     * Updates all four cards using the summary returned by AnalyticsReport.
     *
     * @param summary summary statistics to display
     */
    public void setSummary(Summary summary) {
        Objects.requireNonNull(summary, "Summary cannot be null");

        revenueCard.setValue(Money.format(summary.revenue()));
        ordersCard.setValue(String.valueOf(summary.orders()));
        averageCard.setValue(Money.format(summary.averageOrder()));
        itemsCard.setValue(String.valueOf(summary.itemsSold()));
    }
}
