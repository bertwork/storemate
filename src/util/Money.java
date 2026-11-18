package util;

import java.math.BigDecimal;
import java.util.Locale;

public class Money {

    public static String format(BigDecimal amount) {
        boolean whole = amount.stripTrailingZeros().scale() <= 0;
        return String.format(Locale.US, whole ? "₱%,.0f" : "₱%,.2f", amount);
    }
}