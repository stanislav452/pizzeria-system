package ua.edu.pizzeria;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Спільна JDBC-реалізація {@link CRUDService} за патерном Template Method.
 *
 * <p>Шаблонний метод ({@link #findById}, {@link #findAll}, {@link #save},
 * {@link #deleteById}, {@link #findPage}) містить однакову роботу з
 * Connection/PreparedStatement/ResultSet, а підклас лише надає SQL і
 * перетворення рядка на об'єкт.</p>
 *
 * <p><b>Конвенція для {@link #setParameters(PreparedStatement, BaseEntity)}:</b>
 * SQL для INSERT і UPDATE мають однаковий порядок параметрів, а {@code id}
 * завжди стоїть останнім. Метод {@code setParameters} заповнює всі
 * плейсхолдери, включно з id.</p>
 */
public abstract class AbstractJdbcCrudService<T extends BaseEntity> implements CRUDService<T> {
    protected final DataSource dataSource;

    protected AbstractJdbcCrudService(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource не може бути null");
    }

    protected abstract String sqlFindById();

    protected abstract String sqlFindAll();

    protected abstract String sqlInsert();

    protected abstract String sqlUpdate();

    protected abstract String sqlDelete();

    /** Перетворює поточний рядок ResultSet на сутність. */
    protected abstract T mapRow(ResultSet resultSet) throws SQLException;

    /** Заповнює всі параметри INSERT/UPDATE; id — останній параметр. */
    protected abstract void setParameters(PreparedStatement statement, T entity) throws SQLException;

    @Override
    public T findById(long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlFindById())) {

            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) throw new EntityNotFoundException(id);
                return mapRow(resultSet);
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Помилка пошуку сутності з id=" + id, exception);
        }
    }

    @Override
    public List<T> findAll() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlFindAll());
             ResultSet resultSet = statement.executeQuery()) {

            return mapAll(resultSet);
        } catch (SQLException exception) {
            throw new DataAccessException("Помилка читання списку сутностей", exception);
        }
    }

    @Override
    public T save(T entity) {
        Objects.requireNonNull(entity, "entity не може бути null");

        try (Connection connection = dataSource.getConnection()) {
            int updatedRows;
            try (PreparedStatement statement = connection.prepareStatement(sqlUpdate())) {
                setParameters(statement, entity);
                updatedRows = statement.executeUpdate();
            }

            if (updatedRows == 0) {
                try (PreparedStatement statement = connection.prepareStatement(sqlInsert())) {
                    setParameters(statement, entity);
                    statement.executeUpdate();
                }
            }
            return entity;
        } catch (SQLException exception) {
            throw new DataAccessException("Помилка збереження сутності з id=" + entity.getId(), exception);
        }
    }

    @Override
    public void deleteById(long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlDelete())) {

            statement.setLong(1, id);
            if (statement.executeUpdate() == 0) throw new EntityNotFoundException(id);
        } catch (SQLException exception) {
            throw new DataAccessException("Помилка видалення сутності з id=" + id, exception);
        }
    }

    /**
     * Для довільного Comparator сортування виконується в пам'яті, бо
     * універсальний SQL для будь-якого Java-компаратора побудувати неможливо.
     * Спеціалізовані методи сервісів (наприклад,
     * {@code findAllSortedByNameAndByQuantity()}) сортують через ORDER BY.
     */
    @Override
    public List<T> findAllSorted(Comparator<? super T> comparator) {
        List<T> result = findAll();
        result.sort(Objects.requireNonNull(comparator, "comparator не може бути null"));
        return result;
    }

    /**
     * Посторінкове читання через LIMIT/OFFSET.
     *
     * @param offset кількість рядків, які треба пропустити (>= 0)
     * @param limit  максимальна кількість рядків на сторінці (> 0)
     */
    public List<T> findPage(int offset, int limit) {
        if (offset < 0) throw new IllegalArgumentException("offset має бути >= 0");
        if (limit <= 0) throw new IllegalArgumentException("limit має бути > 0");

        String sql = sqlFindAll() + " LIMIT ? OFFSET ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, limit);
            statement.setInt(2, offset);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapAll(resultSet);
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Помилка посторінкового читання", exception);
        }
    }

    protected List<T> mapAll(ResultSet resultSet) throws SQLException {
        List<T> result = new ArrayList<>();
        while (resultSet.next()) result.add(mapRow(resultSet));
        return result;
    }
}
