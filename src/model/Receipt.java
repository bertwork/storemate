package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Everything shown on the receipt after an order is placed. */
public record Receipt(
        long orderId,
        LocalDateTime placedAt,
        Map<Product, Integer> items,
        String discountLabel,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal total,
        BigDecimal amountReceived,
        BigDecimal change) {

    public static Receipt of(long orderId, Order order, BigDecimal amountReceived) {
        return new Receipt(
                orderId,
                LocalDateTime.now(),
                Collections.unmodifiableMap(new LinkedHashMap<>(order.items())), // keeps the order
                order.discountLabel(),
                order.subtotal(),
                order.discount(),
                order.total(),
                amountReceived,
                amountReceived.subtract(order.total()));
    }
}