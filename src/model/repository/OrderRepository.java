package model.repository;

import model.Order;
import model.Product;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Objects;

public class OrderRepository {

    private static final String INSERT_ORDER = """
            INSERT INTO orders (subtotal, discount_label, discount, total, amount_received, change_amount)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    private static final String INSERT_ITEM = """
            INSERT INTO orders_item (order_id, product_id, quantity, price, line_total)
            VALUES (?, ?, ?, ?, ?)
            """;

    /**
     * Saves an order and all its items in one database transaction.
     * If any operation fails, the transaction is rolled back so that
     * an incomplete order is never stored.
     * Returns the generated order ID after a successful commit.
     */
    public long save(Order order, BigDecimal amountReceived) throws SQLException {

        // Validate input before opening a database connection.
        validateOrder(order, amountReceived);

        BigDecimal change = amountReceived.subtract(order.total());

        try (Connection con = Database.connect()) {
            // Disable auto-commit so the order and its items are saved together.
            con.setAutoCommit(false);

            try {
                long orderId = insertOrder(con, order, amountReceived, change);
                insertItems(con, orderId, order);

                // Commit only after both the order and its items are inserted.
                con.commit();

                return orderId;

            } catch (SQLException | RuntimeException e) {
                // Preserve the original exception if rollback also fails.
                rollback(con, e);
                throw e;
            }
        }
    }

    /**
     * Checks that the order contains valid data before saving it.
     */
    private void validateOrder(Order order, BigDecimal amountReceived) {
        Objects.requireNonNull(order, "Order must not be null.");
        Objects.requireNonNull(amountReceived, "Amount received must not be null.");

        if (order.isEmpty()) {
            throw new IllegalArgumentException("Cannot save an empty order.");
        }

        if (amountReceived.compareTo(order.total()) < 0) {
            throw new IllegalArgumentException("Amount received cannot be less than the order total.");
        }

        // Ensure every order item has a valid product and quantity.
        for (Map.Entry<Product, Integer> line : order.items().entrySet()) {
            Product product = Objects.requireNonNull(line.getKey(), "Order contains a null product.");
            Integer quantity = Objects.requireNonNull(line.getValue(), "Order contains a null quantity.");
            Objects.requireNonNull(product.price(), "Product price must not be null.");

            if (quantity <= 0) {
                throw new IllegalArgumentException("Product quantity must be greater than zero.");
            }

            if (product.price().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Product price cannot be negative.");
            }
        }
    }

    /**
     * Attempts to roll back the transaction.
     * Any rollback exception is attached to the original exception
     * instead of replacing it.
     */
    private void rollback(Connection con, Exception originalException) {
        try {
            con.rollback();
        } catch (SQLException rollbackException) {
            // Keep the original database error as the primary exception.
            originalException.addSuppressed(rollbackException);
        }
    }

    /**
     * Inserts the main order record and retrieves its generated ID.
     */
    private long insertOrder(
            Connection con,
            Order order,
            BigDecimal received,
            BigDecimal change
    ) throws SQLException {

        try (PreparedStatement st = con.prepareStatement(INSERT_ORDER, Statement.RETURN_GENERATED_KEYS)) {

            st.setBigDecimal(1, order.subtotal());
            st.setString(2, order.discountLabel());
            st.setBigDecimal(3, order.discount());
            st.setBigDecimal(4, order.total());
            st.setBigDecimal(5, received);
            st.setBigDecimal(6, change);

            st.executeUpdate();

            try (ResultSet keys = st.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No order ID was generated.");
                }

                return keys.getLong(1);
            }
        }
    }

    /**
     * Inserts all products belonging to the order using batch execution.
     * The product price is stored at the time of sale so that
     * historical order records are not affected by future price changes.
     */
    private void insertItems(Connection con, long orderId, Order order) throws SQLException {

        try (PreparedStatement st = con.prepareStatement(INSERT_ITEM)) {

            for (Map.Entry<Product, Integer> line : order.items().entrySet()) {

                Product product = line.getKey();
                int quantity = line.getValue();

                st.setLong(1, orderId);
                st.setInt(2, product.id());
                st.setInt(3, quantity);
                st.setBigDecimal(4, product.price());

                // Calculate the line total using the sale-time product price.
                BigDecimal lineTotal = product.price().multiply(BigDecimal.valueOf(quantity));
                st.setBigDecimal(5, lineTotal);
                st.addBatch();
            }

            st.executeBatch();
        }
    }
}
