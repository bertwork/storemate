package view.panels;

import model.Product;
import util.Theme;
import view.components.products.CategoryTabs;
import view.components.products.ProductCard;
import view.components.products.SearchBar;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class ProductsPanel extends JPanel {

    private static final String ALL = "All";

    private final JPanel grid = new JPanel(new GridLayout(0, 4, 16, 16));
    private final Map<Product, ProductCard> cards = new LinkedHashMap<>();

    private String selectedCategory = ALL;
    private String query = "";

    private final Consumer<Product> onAdd;
    private final List<Product> matches = new ArrayList<>(); // products currently shown in the grid
    private final SearchBar searchBar = new SearchBar("Search Product", text -> {
        query = text;
        applyFilter();
    }, this::addSingleMatch);

    // the grid, or a message when nothing matches
    private static final String GRID = "grid";
    private static final String NO_RESULTS = "no-results";
    private final CardLayout resultsLayout = new CardLayout();
    private final JPanel results = new JPanel(resultsLayout);
    private final JLabel noResultsTitle = new JLabel();
    private final JLabel noResultsHint = new JLabel();

    public ProductsPanel(List<Product> products, Consumer<Product> onAdd) {
        setLayout(new BorderLayout(0, 8));
        this.onAdd = onAdd;

        for (Product product : products) {
            cards.put(product, new ProductCard(product, () -> onAdd.accept(product)));
        }

        add(buildTop(), BorderLayout.NORTH);
        add(buildResults(), BorderLayout.CENTER);
        applyFilter();
    }

    // called by OrderPanel so the card matches the order state
    public void setAdded(Product product, boolean added) {
        ProductCard card = cards.get(product);
        if (card != null) card.setAdded(added);
    }

    private JPanel buildTop() {
        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        top.add(searchBar, BorderLayout.NORTH);

        top.add(new CategoryTabs(categoryTabs(), category -> {
            selectedCategory = category;
            applyFilter();
        }), BorderLayout.SOUTH);
        return top;
    }

    // "All" plus each category, in the order the products come in
    private String[] categoryTabs() {
        List<String> names = new ArrayList<>();
        names.add(ALL);
        cards.keySet().stream().map(Product::category).distinct().forEach(names::add);
        return names.toArray(new String[0]);
    }

    private JPanel buildResults() {
        results.setOpaque(false);
        results.add(buildGrid(), GRID);
        results.add(buildNoResults(), NO_RESULTS);
        return results;
    }

    private JPanel buildNoResults() {
        noResultsTitle.setFont(Theme.bold(noResultsTitle, 16f));
        noResultsTitle.setForeground(Theme.NAVY);
        noResultsTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        noResultsHint.setFont(Theme.regular(noResultsHint, 13f));
        noResultsHint.setForeground(Theme.MUTED);
        noResultsHint.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.add(noResultsTitle);
        box.add(Box.createVerticalStrut(4));
        box.add(noResultsHint);

        JPanel wrap = new JPanel(new GridBagLayout()); // GridBag centers its single child
        wrap.setOpaque(false);
        wrap.add(box);
        return wrap;
    }

    private JScrollPane buildGrid() {
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 8)); // 8 on the right leaves room for the scrollbar

        // wrapper keeps cards at normal height instead of stretching to fill
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(grid, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    // GridLayout still reserves a cell for hidden components, so rebuild the grid instead
    private void applyFilter() {
        String needle = query.toLowerCase(Locale.ROOT);

        grid.removeAll();
        int shown = 0;
        matches.clear();
        for (Map.Entry<Product, ProductCard> entry : cards.entrySet()) {
            Product p = entry.getKey();
            boolean inCategory = selectedCategory.equals(ALL) || p.category().equals(selectedCategory);
            boolean matchesSearch = p.name().toLowerCase(Locale.ROOT).contains(needle);
            if (inCategory && matchesSearch) {
                grid.add(entry.getValue());
                matches.add(p);
                shown++;
            }
        }
        grid.revalidate();
        grid.repaint();
        showResults(shown);
    }

    public void focusSearch() {
        searchBar.requestFocusInWindow();
    }

    // Enter in the search box: if exactly one product matches, add it and clear the search
    private void addSingleMatch() {
        if (matches.size() != 1) return;
        Product product = matches.get(0);
        if (cards.get(product).isAdded()) return;

        onAdd.accept(product);
        searchBar.setText("");
    }

    private void showResults(int shown) {
        if (shown > 0) {
            resultsLayout.show(results, GRID);
            return;
        }

        if (cards.isEmpty()) {
            noResultsTitle.setText("No products available");
            noResultsHint.setText("Add products to the database to see them here");
        } else if (!query.isBlank()) {
            String where = selectedCategory.equals(ALL) ? "" : " in " + selectedCategory;
            noResultsTitle.setText("No products found");
            noResultsHint.setText("Nothing matches “" + query + "”" + where + ". Try a different name.");
        } else {
            noResultsTitle.setText("No products in " + selectedCategory);
            noResultsHint.setText("Pick another category");
        }
        resultsLayout.show(results, NO_RESULTS);
    }
}