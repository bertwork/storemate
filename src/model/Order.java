package model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Holds what is currently in the checkout. Knows nothing about Swing. */
public class Order {

    private final Map<Product, Integer> items = new LinkedHashMap<>();
    private BigDecimal discount = BigDecimal.ZERO;
    private String discountLabel = ""; // e.g. "Senior 20%", saved with the order

    public void add(Product product) {
        items.putIfAbsent(product, 1);
    }

    /** quantity <= 0 removes the item */
    public void setQuantity(Product product, int quantity) {
        if (quantity <= 0) items.remove(product);
        else items.put(product, quantity);
    }

    public void setDiscount(BigDecimal discount) {
        setDiscount("", discount);
    }

    public void setDiscount(String label, BigDecimal discount) {
        this.discountLabel = label == null ? "" : label;
        this.discount = discount.max(BigDecimal.ZERO);
    }

    public String discountLabel() {
        return discountLabel;
    }

    /** Independent snapshot, so saving in the background can't be affected by later edits. */
    public Order copy() {
        Order c = new Order();
        c.items.putAll(items);
        c.discount = discount;
        c.discountLabel = discountLabel;
        return c;
    }

    public void clear() {
        items.clear();
        discount = BigDecimal.ZERO;
        discountLabel = "";
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public Map<Product, Integer> items() {
        return Collections.unmodifiableMap(items);
    }

    public BigDecimal subtotal() {
        return items.entrySet().stream()
                .map(e -> e.getKey().price().multiply(BigDecimal.valueOf(e.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** never more than the subtotal, so the total can't go negative */
    public BigDecimal discount() {
        return discount.min(subtotal());
    }

    public BigDecimal total() {
        return subtotal().subtract(discount());
    }
}