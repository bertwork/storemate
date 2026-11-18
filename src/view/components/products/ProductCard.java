package view.components.products;

import model.Product;
import util.ImageLoader;
import util.Money;
import util.Theme;
import view.components.RoundedPanel;
import view.components.common.ProductImage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

/**
 * Displays a product's image, name, category, and price.
 * Handles product selection and added-state presentation.
 */
public class ProductCard extends RoundedPanel {

    private static final int IMAGE_HEIGHT = 120;

    private final Product product;
    private final Runnable onClick;

    private final ProductImage image = new ProductImage(10, true);
    private final JLabel nameLabel = new JLabel();
    private final JLabel addedLabel = new JLabel("✓ Added");

    private boolean added = false;
    private boolean hovered = false;

    public ProductCard(Product product, Runnable onClick) {
        super(Theme.SURFACE, Theme.BORDER, 14);

        this.product = Objects.requireNonNull(product, "Product cannot be null.");
        this.onClick = Objects.requireNonNull(onClick, "Click action cannot be null.");

        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 12, 10));

        // Product image
        image.setPreferredSize(new Dimension(0, IMAGE_HEIGHT));
        image.setImage(
                ImageLoader.productImage(
                        product.imagePath(),
                        product.category()
                )
        );

        // Product details
        nameLabel.setText(product.name());
        nameLabel.setFont(Theme.bold(nameLabel, 14f));
        nameLabel.setForeground(Theme.NAVY);

        JLabel categoryLabel = new JLabel(product.category());
        categoryLabel.setFont(Theme.regular(categoryLabel, 12f));
        categoryLabel.setForeground(Theme.MUTED);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(nameLabel);
        text.add(Box.createVerticalStrut(2));
        text.add(categoryLabel);

        // Product price
        JLabel priceLabel = new JLabel(Money.format(product.price()));
        priceLabel.setFont(Theme.bold(priceLabel, 16f));
        priceLabel.setForeground(Theme.ACCENT);

        // Added indicator
        addedLabel.setFont(Theme.bold(addedLabel, 12f));
        addedLabel.setForeground(Theme.SUCCESS);
        addedLabel.setVisible(false);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(priceLabel, BorderLayout.WEST);
        bottom.add(addedLabel, BorderLayout.EAST);

        JPanel info = new JPanel(new BorderLayout(0, 8));
        info.setOpaque(false);
        info.add(text, BorderLayout.NORTH);
        info.add(bottom, BorderLayout.SOUTH);

        add(image, BorderLayout.NORTH);
        add(info, BorderLayout.CENTER);

        installInteractionListeners(this);
    }

    /**
     * Registers mouse interactions on the card and its child components.
     */
    private void installInteractionListeners(Component component) {
        MouseAdapter mouseListener = new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                updateBorder();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                updateBorder();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!added && SwingUtilities.isLeftMouseButton(e)) {
                    onClick.run();
                }
            }
        };

        component.addMouseListener(mouseListener);

        if (component != this) {
            component.setCursor(
                    Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            );
        }

        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                installInteractionListeners(child);
            }
        }
    }

    public Product getProduct() {
        return product;
    }

    public boolean isAdded() {
        return added;
    }

    /**
     * Updates the card appearance based on its order state.
     */
    public void setAdded(boolean added) {
        this.added = added;

        addedLabel.setVisible(added);
        image.setFaded(added);

        nameLabel.setForeground(
                added ? Theme.MUTED : Theme.NAVY
        );

        setFill(
                added ? Theme.CARD_ADDED : Theme.SURFACE
        );

        updateCursor(this);
        updateBorder();
    }

    private void updateCursor(Component component) {
        component.setCursor(
                Cursor.getPredefinedCursor(
                        added ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR
                )
        );

        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                updateCursor(child);
            }
        }
    }

    /**
     * Updates the border based on the hover and added states.
     */
    private void updateBorder() {
        setBorderColor(
                hovered && !added
                        ? Theme.ACCENT
                        : Theme.BORDER
        );
    }
}
