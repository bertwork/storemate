package model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Holds what is currently in the checkout. Knows nothing about Swing. */
public class Order {

    private final Map<Product, Integer> items = new LinkedHashMap<>();

    public void add(Product product) {
        items.putIfAbsent(product, 1);
    }

    /** quantity <= 0 removes the item */
    public void setQuantity(Product product, int quantity) {
        if (quantity <= 0) items.remove(product);
        else items.put(product, quantity);
    }

    /** Independent snapshot, so saving in the background can't be affected by later edits. */
    public Order copy() {
        Order c = new Order();
        c.items.putAll(items);
        return c;
    }

    public void clear() {
        items.clear();
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

    public BigDecimal total() {
        return subtotal();
    }
}