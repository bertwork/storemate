package model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Everything the Analytics screen shows for one date range. Plain data, no Swing. */
public record AnalyticsReport(
        Summary summary,
        List<Point> salesOverTime,
        List<CategorySales> salesByCategory,
        List<ProductSales> topProducts) {

    public record Summary(int orders, BigDecimal revenue, int itemsSold) {
        public BigDecimal averageOrder() {
            return orders == 0
                    ? BigDecimal.ZERO
                    : revenue.divide(BigDecimal.valueOf(orders), 2, RoundingMode.HALF_UP);
        }
    }

    /** One bar of the sales chart: an hour, a day or a month. */
    public record Point(String label, BigDecimal amount) {}

    public record CategorySales(String category, BigDecimal amount) {}

    public record ProductSales(String name, int units, BigDecimal revenue) {}
}