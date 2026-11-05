package ua.edu.pizzeria;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Операція, що змінює дві таблиці (pizzas та orders) в одній транзакції:
 * спочатку додає піцу в меню, потім створює замовлення на неї.
 *
 * <p>Якщо друга вставка падає (наприклад, порушено FOREIGN KEY або CHECK),
 * виконується rollback, і піца з першої вставки теж не залишається в БД.</p>
 */
public class JdbcOrderPlacementService {
    private final DataSource dataSource;

    public JdbcOrderPlacementService(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource не може бути null");
    }

    public void createPizzaWithOrder(Pizza pizza, Order order) {
        Objects.requireNonNull(pizza, "pizza не може бути null");
        Objects.requireNonNull(order, "order не може бути null");

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                insertPizza(connection, pizza);
                insertOrder(connection, order);
                connection.commit();
            } catch (SQLException exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }
                throw new DataAccessException(
                        "Транзакцію скасовано (rollback): піцу та замовлення не збережено", exception);
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException ignored) {
                    // з'єднання все одно буде повернуто в пул
                }
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Помилка роботи з БД під час створення замовлення", exception);
        }
    }

    private static void insertPizza(Connection connection, Pizza pizza) throws SQLException {
        String sql = "INSERT INTO pizzas (name, price, diameter_cm, id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, pizza.getName());
            statement.setBigDecimal(2, BigDecimal.valueOf(pizza.getPrice()));
            statement.setInt(3, pizza.getDiameterCm());
            statement.setLong(4, pizza.getId());
            statement.executeUpdate();
        }
    }

    private static void insertOrder(Connection connection, Order order) throws SQLException {
        String sql = "INSERT INTO orders (name, quantity, extra_sauce, id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, order.getName());
            statement.setInt(2, order.getQuantity());
            statement.setBoolean(3, order.hasExtraSauce());
            statement.setLong(4, order.getId());
            statement.executeUpdate();
        }
    }
}
