package view.components.analytics;

import model.AnalyticsReport.ProductSales;
import util.Money;
import util.Theme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.List;
import java.util.Objects;

/**
 * Displays the top-selling products in a table.
 * If there are no products to display, an empty-state message is shown.
 * This component only handles presentation and does not retrieve data.
 */
public class TopProductsPanel extends JPanel {

    private static final String TABLE = "table";
    private static final String NO_SALES = "no-sales";

    // Manages switching between the products table and empty state.
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);

    // Table model prevents users from editing product information.
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"#", "Product", "Units sold", "Revenue"},
            0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(tableModel);

    public TopProductsPanel() {
        setLayout(new BorderLayout());

        // Let the parent SectionCard provide the background color.
        setOpaque(false);

        configureTable();

        content.setOpaque(false);
        content.add(createTablePanel(), TABLE);
        content.add(createEmptyState(), NO_SALES);

        add(content, BorderLayout.CENTER);

        // Initially show the empty state until data is supplied.
        cardLayout.show(content, NO_SALES);
    }

    /**
     * Updates the table using the supplied top-selling products.
     * @param products products sorted by sales performance
     */
    public void setProducts(List<ProductSales> products) {
        Objects.requireNonNull(products, "Products cannot be null");

        tableModel.setRowCount(0);

        int rank = 1;

        for (ProductSales product : products) {
            tableModel.addRow(new Object[]{
                    rank++,
                    product.name(),
                    product.units(),
                    Money.format(product.revenue())
            });
        }

        // Show the empty state when there are no products.
        cardLayout.show(content, products.isEmpty() ? NO_SALES : TABLE);
    }

    /**
     * Configures the table appearance and column alignment.
     */
    private void configureTable() {
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        // The table is display-only; users cannot select its rows.
        table.setRowSelectionAllowed(false);
        table.setFocusable(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));

        // Keep the ranking column compact.
        TableColumn rankColumn = table.getColumnModel().getColumn(0);
        rankColumn.setMaxWidth(48);

        // Align numeric values to the right.
        rightAlign(table, 2);
        rightAlign(table, 3);
    }

    /**
     * Creates the scrollable products table.
     */
    private JScrollPane createTablePanel() {
        JScrollPane scrollPane = new JScrollPane(table);

        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Theme.SURFACE);

        return scrollPane;
    }

    /**
     * Creates the message displayed when there are no products to show.
     */
    private JPanel createEmptyState() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("No sales in this range");
        title.setFont(font(title, Font.BOLD, 15f));
        title.setForeground(Theme.NAVY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel hint = new JLabel("Try a different date range");
        hint.setFont(font(hint, Font.PLAIN, 12f));
        hint.setForeground(Theme.MUTED);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(title);
        panel.add(Box.createVerticalStrut(6));
        panel.add(hint);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    /**
     * Applies right alignment to a table column and its header.
     */
    private static void rightAlign(JTable table, int column) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.RIGHT);

        table.getColumnModel()
                .getColumn(column)
                .setCellRenderer(renderer);

        TableCellRenderer headerRenderer =
                table.getTableHeader().getDefaultRenderer();

        table.getColumnModel()
                .getColumn(column)
                .setHeaderRenderer((tbl, value, isSelected, hasFocus, row, col) -> {
                    Component component = headerRenderer.getTableCellRendererComponent(
                            tbl,
                            value,
                            isSelected,
                            hasFocus,
                            row,
                            col
                    );

                    if (component instanceof JLabel label) {
                        label.setHorizontalAlignment(SwingConstants.RIGHT);
                    }

                    return component;
                });
    }

    /**
     * Creates a font using the existing table font family.
     */
    private static Font font(JComponent component, int style, float size) {
        return component.getFont().deriveFont(style, size);
    }
}