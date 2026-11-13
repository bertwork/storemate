package model.repository;

import model.AnalyticsReport;
import model.AnalyticsReport.CategorySales;
import model.AnalyticsReport.Point;
import model.AnalyticsReport.ProductSales;
import model.AnalyticsReport.Summary;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AnalyticsRepository {

    // every query uses the same range: from <= created_at < (to + 1 day)
    private static final String SUMMARY = """
            SELECT COUNT(*) AS order_count, COALESCE(SUM(total), 0) AS revenue
            FROM orders
            WHERE created_at >= ? AND created_at < ?
            """;

    private static final String ITEMS_SOLD = """
            SELECT COALESCE(SUM(oi.quantity), 0) AS items
            FROM orders_item oi
            JOIN orders o ON o.id = oi.order_id
            WHERE o.created_at >= ? AND o.created_at < ?
            """;

    private static final String BY_CATEGORY = """
            SELECT c.category_name, SUM(oi.line_total) AS amount
            FROM orders_item oi
            JOIN orders o ON o.id = oi.order_id
            JOIN products p ON p.id = oi.product_id
            JOIN categories c ON c.id = p.category_id
            WHERE o.created_at >= ? AND o.created_at < ?
            GROUP BY c.id, c.category_name
            ORDER BY amount DESC
            """;

    private static final String TOP_PRODUCTS = """
            SELECT p.product_name, SUM(oi.quantity) AS units, SUM(oi.line_total) AS revenue
            FROM orders_item oi
            JOIN orders o ON o.id = oi.order_id
            JOIN products p ON p.id = oi.product_id
            WHERE o.created_at >= ? AND o.created_at < ?
            GROUP BY p.id, p.product_name
            ORDER BY units DESC, revenue DESC
            LIMIT 10
            """;

    // how the sales chart is grouped, depending on how long the range is
    private enum Granularity {
        HOUR("%H"), DAY("%Y-%m-%d"), MONTH("%Y-%m");

        final String sqlFormat;

        Granularity(String sqlFormat) { this.sqlFormat = sqlFormat; }
    }

    private static final DateTimeFormatter HOUR_LABEL  = DateTimeFormatter.ofPattern("h a", Locale.ENGLISH);
    private static final DateTimeFormatter DAY_LABEL   = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);
    private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);

    public AnalyticsReport load(LocalDate from, LocalDate to) throws SQLException {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay(); // exclusive

        try (Connection con = Database.connect()) {
            return new AnalyticsReport(
                    summary(con, start, end),
                    salesOverTime(con, from, to, start, end),
                    salesByCategory(con, start, end),
                    topProducts(con, start, end));
        }
    }

    private Summary summary(Connection con, LocalDateTime start, LocalDateTime end) throws SQLException {
        int orders;
        BigDecimal revenue;
        try (PreparedStatement st = ranged(con, SUMMARY, start, end);
             ResultSet rs = st.executeQuery()) {
            rs.next();
            orders = rs.getInt("order_count");
            revenue = rs.getBigDecimal("revenue");
        }

        int items;
        try (PreparedStatement st = ranged(con, ITEMS_SOLD, start, end);
             ResultSet rs = st.executeQuery()) {
            rs.next();
            items = rs.getInt("items");
        }
        return new Summary(orders, revenue, items);
    }

    private List<Point> salesOverTime(Connection con, LocalDate from, LocalDate to,
                                      LocalDateTime start, LocalDateTime end) throws SQLException {
        Granularity g = granularityFor(from, to);
        String sql = "SELECT DATE_FORMAT(created_at, '" + g.sqlFormat + "') AS bucket, SUM(total) AS amount "
                + "FROM orders WHERE created_at >= ? AND created_at < ? "
                + "GROUP BY bucket ORDER BY bucket";

        Map<String, BigDecimal> totals = new HashMap<>();
        try (PreparedStatement st = ranged(con, sql, start, end);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) totals.put(rs.getString("bucket"), rs.getBigDecimal("amount"));
        }

        // walk every bucket in the range so days/hours/months without sales show as 0
        List<Point> points = new ArrayList<>();
        switch (g) {
            case HOUR -> {
                for (int h = 0; h < 24; h++) {
                    points.add(new Point(LocalTime.of(h, 0).format(HOUR_LABEL),
                            totals.getOrDefault(String.format("%02d", h), BigDecimal.ZERO)));
                }
            }
            case DAY -> {
                for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
                    points.add(new Point(d.format(DAY_LABEL),
                            totals.getOrDefault(d.toString(), BigDecimal.ZERO)));
                }
            }
            case MONTH -> {
                YearMonth last = YearMonth.from(to);
                for (YearMonth m = YearMonth.from(from); !m.isAfter(last); m = m.plusMonths(1)) {
                    points.add(new Point(m.atDay(1).format(MONTH_LABEL),
                            totals.getOrDefault(m.toString(), BigDecimal.ZERO)));
                }
            }
        }
        return points;
    }

    private List<CategorySales> salesByCategory(Connection con, LocalDateTime start, LocalDateTime end)
            throws SQLException {
        List<CategorySales> result = new ArrayList<>();
        try (PreparedStatement st = ranged(con, BY_CATEGORY, start, end);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                result.add(new CategorySales(rs.getString("category_name"), rs.getBigDecimal("amount")));
            }
        }
        return result;
    }

    private List<ProductSales> topProducts(Connection con, LocalDateTime start, LocalDateTime end)
            throws SQLException {
        List<ProductSales> result = new ArrayList<>();
        try (PreparedStatement st = ranged(con, TOP_PRODUCTS, start, end);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                result.add(new ProductSales(rs.getString("product_name"),
                        rs.getInt("units"), rs.getBigDecimal("revenue")));
            }
        }
        return result;
    }

    private static Granularity granularityFor(LocalDate from, LocalDate to) {
        if (from.equals(to)) return Granularity.HOUR;
        return ChronoUnit.DAYS.between(from, to) <= 92 ? Granularity.DAY : Granularity.MONTH;
    }

    private static PreparedStatement ranged(Connection con, String sql, LocalDateTime start, LocalDateTime end)
            throws SQLException {
        PreparedStatement st = con.prepareStatement(sql);
        st.setObject(1, start);
        st.setObject(2, end);
        return st;
    }
}