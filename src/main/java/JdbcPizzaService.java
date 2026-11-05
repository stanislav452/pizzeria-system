package ua.edu.pizzeria;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Ручна JDBC-реалізація {@link PizzaService} (рівень 1).
 * Друга сутність (Order) реалізована вже через {@link AbstractJdbcCrudService}.
 *
 * <p>Гроші в БД зберігаються як NUMERIC(10,2), тому назовні повертаються
 * через {@link BigDecimal#doubleValue()} без накопичення похибки FLOAT.</p>
 */
public class JdbcPizzaService implements PizzaService {
    private static final String COLUMNS = "id, name, price, diameter_cm";

    private final DataSource dataSource;

    public JdbcPizzaService(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource не може бути null");
    }

    @Override
    public Pizza findById(long id) {
        String sql = "SELECT " + COLUMNS + " FROM pizzas WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) throw new EntityNotFoundException(id);
                return mapRow(resultSet);
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Помилка пошуку піци з id=" + id, exception);
        }
    }

    @Override
    public List<Pizza> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM pizzas ORDER BY id";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            return mapAll(resultSet);
        } catch (SQLException exception) {
            throw new DataAccessException("Помилка читання списку піц", exception);
        }
    }

    @Override
    public Pizza save(Pizza pizza) {
        Objects.requireNonNull(pizza, "pizza не може бути null");

        String updateSql = "UPDATE pizzas SET name = ?, price = ?, diameter_cm = ? WHERE id = ?";
        String insertSql = "INSERT INTO pizzas (name, price, diameter_cm, id) VALUES (?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection()) {
            int updatedRows;
            try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
                setParameters(statement, pizza);
                updatedRows = statement.executeUpdate();
            }
            if (updatedRows == 0) {
                try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
                    setParameters(statement, pizza);
                    statement.executeUpdate();
                }
            }
            return pizza;
        } catch (SQLException exception) {
            throw new DataAccessException("Помилка збереження піци з id=" + pizza.getId(), exception);
        }
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM pizzas WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            if (statement.executeUpdate() == 0) throw new EntityNotFoundException(id);
        } catch (SQLException exception) {
            throw new DataAccessException("Помилка видалення піци з id=" + id, exception);
        }
    }

    @Override
    public List<Pizza> findAllSorted(Comparator<? super Pizza> comparator) {
        List<Pizza> result = findAll();
        result.sort(Objects.requireNonNull(comparator, "comparator не може бути null"));
        return result;
    }

    /** Сортування виконується в БД через ORDER BY, а не в пам'яті. */
    @Override
    public List<Pizza> findAllSortedByNameAndByPrice() {
        String sql = "SELECT " + COLUMNS + " FROM pizzas ORDER BY name, price";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            return mapAll(resultSet);
        } catch (SQLException exception) {
            throw new DataAccessException("Помилка сортування піц за назвою та ціною", exception);
        }
    }

    /**
     * Безпечний пошук за назвою: назва передається як параметр PreparedStatement
     * і ніколи не стає частиною SQL-тексту.
     */
    public List<Pizza> findByName(String name) {
        Objects.requireNonNull(name, "name не може бути null");
        String sql = "SELECT " + COLUMNS + " FROM pizzas WHERE name = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapAll(resultSet);
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Помилка пошуку піци за назвою", exception);
        }
    }

    /** Посторінкове читання: LIMIT/OFFSET. */
    public List<Pizza> findPage(int offset, int limit) {
        if (offset < 0) throw new IllegalArgumentException("offset має бути >= 0");
        if (limit <= 0) throw new IllegalArgumentException("limit має бути > 0");

        String sql = "SELECT " + COLUMNS + " FROM pizzas ORDER BY id LIMIT ? OFFSET ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, limit);
            statement.setInt(2, offset);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapAll(resultSet);
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Помилка посторінкового читання піц", exception);
        }
    }

    private static Pizza mapRow(ResultSet resultSet) throws SQLException {
        return new Pizza(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getBigDecimal("price").doubleValue(),
                resultSet.getInt("diameter_cm"));
    }

    private static List<Pizza> mapAll(ResultSet resultSet) throws SQLException {
        List<Pizza> pizzas = new ArrayList<>();
        while (resultSet.next()) pizzas.add(mapRow(resultSet));
        return pizzas;
    }

    private static void setParameters(PreparedStatement statement, Pizza pizza) throws SQLException {
        statement.setString(1, pizza.getName());
        statement.setBigDecimal(2, BigDecimal.valueOf(pizza.getPrice()));
        statement.setInt(3, pizza.getDiameterCm());
        statement.setLong(4, pizza.getId());
    }
}
