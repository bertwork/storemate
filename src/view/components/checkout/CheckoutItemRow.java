package view.components.checkout;

import model.Product;
import util.ImageLoader;
import util.Money;
import util.Theme;
import view.components.RoundedPanel;
import view.components.common.AppButton;
import view.components.common.ProductImage;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.function.IntConsumer;

/**
 * Displays a product in the checkout list with its quantity and total price.
 */
public class CheckoutItemRow extends RoundedPanel {

    private final BigDecimal unitPrice;
    private final JLabel totalLabel = new JLabel();

    public CheckoutItemRow(Product product, int quantity, IntConsumer onQuantityChange) {
        super(Theme.SURFACE, null, 10);

        this.unitPrice = product.price();

        setLayout(new BorderLayout(10, 0));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(buildThumbnail(product), BorderLayout.WEST);
        add(buildDetails(product, quantity, onQuantityChange), BorderLayout.CENTER);
    }

    // Prevents the row from stretching vertically inside a BoxLayout.
    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    private JPanel buildThumbnail(Product product) {
        ProductImage thumb = new ProductImage(8, true);
        thumb.setImage(ImageLoader.productImage(product.imagePath(), product.category()));
        thumb.setPreferredSize(new Dimension(68, 52));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(thumb, BorderLayout.NORTH);

        return wrap;
    }

    private JPanel buildDetails(Product product, int quantity, IntConsumer onQuantityChange) {
        // Product name and unit price.
        JLabel nameLabel = new JLabel(product.name());
        nameLabel.setFont(Theme.bold(nameLabel, 14f));
        nameLabel.setForeground(Theme.NAVY);

        JLabel eachLabel = new JLabel(Money.format(unitPrice));
        eachLabel.setFont(Theme.regular(eachLabel, 12f));
        eachLabel.setForeground(Theme.MUTED);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(nameLabel);
        text.add(eachLabel);

        // Removes the product from the checkout list.
        AppButton remove = AppButton.icon(
                new TrashIcon(16, Theme.DANGER),
                "Remove",
                e -> onQuantityChange.accept(0)
        );
        remove.setPreferredSize(new Dimension(32, 32));

        JPanel removeWrap = new JPanel(new BorderLayout());
        removeWrap.setOpaque(false);
        removeWrap.add(remove, BorderLayout.NORTH);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(text, BorderLayout.CENTER);
        top.add(removeWrap, BorderLayout.EAST);

        // Quantity control and line total.
        totalLabel.setFont(Theme.bold(totalLabel, 13f));
        updateTotal(quantity);

        JLabel totalText = new JLabel("Total:");
        totalText.setFont(Theme.regular(totalText, 12f));
        totalText.setForeground(Theme.MUTED);

        JPanel total = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        total.setOpaque(false);
        total.add(totalText);
        total.add(totalLabel);

        QuantityStepper stepper = new QuantityStepper(quantity, newQty -> {
            updateTotal(newQty);
            onQuantityChange.accept(newQty);
        });

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(stepper, BorderLayout.WEST);
        bottom.add(total, BorderLayout.EAST);

        JPanel details = new JPanel(new BorderLayout(0, 6));
        details.setOpaque(false);
        details.add(top, BorderLayout.NORTH);
        details.add(bottom, BorderLayout.SOUTH);

        return details;
    }

    private void updateTotal(int quantity) {
        totalLabel.setText(
                Money.format(unitPrice.multiply(BigDecimal.valueOf(quantity)))
        );
    }
}