package ua.edu.pizzeria;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * JDBC-реалізація {@link OrderService} через {@link AbstractJdbcCrudService}
 * (рівень 3). Таблиця orders має зовнішній ключ на pizzas(name):
 * одна піца може входити в багато замовлень.
 */
public class JdbcOrderService extends AbstractJdbcCrudService<Order> implements OrderService {
    private static final String COLUMNS = "id, name, quantity, extra_sauce";

    public JdbcOrderService(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected String sqlFindById() {
        return "SELECT " + COLUMNS + " FROM orders WHERE id = ?";
    }

    @Override
    protected String sqlFindAll() {
        return "SELECT " + COLUMNS + " FROM orders ORDER BY id";
    }

    @Override
    protected String sqlInsert() {
        return "INSERT INTO orders (name, quantity, extra_sauce, id) VALUES (?, ?, ?, ?)";
    }

    @Override
    protected String sqlUpdate() {
        return "UPDATE orders SET name = ?, quantity = ?, extra_sauce = ? WHERE id = ?";
    }

    @Override
    protected String sqlDelete() {
        return "DELETE FROM orders WHERE id = ?";
    }

    @Override
    protected Order mapRow(ResultSet resultSet) throws SQLException {
        return new Order(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getInt("quantity"),
                resultSet.getBoolean("extra_sauce"));
    }

    @Override
    protected void setParameters(PreparedStatement statement, Order order) throws SQLException {
        statement.setString(1, order.getName());
        statement.setInt(2, order.getQuantity());
        statement.setBoolean(3, order.hasExtraSauce());
        statement.setLong(4, order.getId());
    }

    /** Сортування виконується SQL-запитом через ORDER BY name, quantity. */
    @Override
    public List<Order> findAllSortedByNameAndByQuantity() {
        String sql = "SELECT " + COLUMNS + " FROM orders ORDER BY name, quantity";
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql);
             var resultSet = statement.executeQuery()) {

            return mapAll(resultSet);
        } catch (SQLException exception) {
            throw new DataAccessException("Помилка сортування замовлень за назвою та кількістю", exception);
        }
    }
}
