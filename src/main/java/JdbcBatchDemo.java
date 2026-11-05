package ua.edu.pizzeria;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Демонстрація пакетної вставки (addBatch/executeBatch) проти вставки
 * по одному запису. Обидва варіанти використовують PreparedStatement;
 * різниця лише в кількості звернень до БД.
 */
public final class JdbcBatchDemo {
    private static final long SINGLE_START_ID = 100_000L;
    private static final long BATCH_START_ID = 200_000L;

    private JdbcBatchDemo() {
    }

    public static void run(DataSource dataSource, int rowCount) {
        Objects.requireNonNull(dataSource, "dataSource не може бути null");
        if (rowCount <= 0) throw new IllegalArgumentException("rowCount має бути > 0");

        deleteRange(dataSource, SINGLE_START_ID, SINGLE_START_ID + rowCount);
        deleteRange(dataSource, BATCH_START_ID, BATCH_START_ID + rowCount);

        long singleMillis = insertOneByOne(dataSource, rowCount);
        long batchMillis = insertBatch(dataSource, rowCount);

        String speedup = batchMillis == 0
                ? "> " + singleMillis + "x"
                : String.format("%.1fx", (double) singleMillis / batchMillis);

        System.out.printf("Вставка по одному: %d записів за %d мс%n", rowCount, singleMillis);
        System.out.printf("Пакетна вставка:   %d записів за %d мс%n", rowCount, batchMillis);
        System.out.println("Прискорення: " + speedup);

        deleteRange(dataSource, SINGLE_START_ID, SINGLE_START_ID + rowCount);
        deleteRange(dataSource, BATCH_START_ID, BATCH_START_ID + rowCount);
    }

    private static long insertOneByOne(DataSource dataSource, int rowCount) {
        String sql = "INSERT INTO pizzas (name, price, diameter_cm, id) VALUES (?, ?, ?, ?)";
        long start = System.nanoTime();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (int i = 0; i < rowCount; i++) {
                bind(statement, "Batch-single-" + i, 100 + (i % 50), 30, SINGLE_START_ID + i);
                statement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Помилка одиночної вставки", exception);
        }
        return (System.nanoTime() - start) / 1_000_000;
    }

    private static long insertBatch(DataSource dataSource, int rowCount) {
        String sql = "INSERT INTO pizzas (name, price, diameter_cm, id) VALUES (?, ?, ?, ?)";
        long start = System.nanoTime();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (int i = 0; i < rowCount; i++) {
                bind(statement, "Batch-batch-" + i, 100 + (i % 50), 30, BATCH_START_ID + i);
                statement.addBatch();
                if ((i + 1) % 100 == 0) statement.executeBatch();
            }
            statement.executeBatch();
        } catch (SQLException exception) {
            throw new DataAccessException("Помилка пакетної вставки", exception);
        }
        return (System.nanoTime() - start) / 1_000_000;
    }

    private static void bind(PreparedStatement statement, String name, double price,
                             int diameterCm, long id) throws SQLException {
        statement.setString(1, name);
        statement.setBigDecimal(2, BigDecimal.valueOf(price));
        statement.setInt(3, diameterCm);
        statement.setLong(4, id);
    }

    private static void deleteRange(DataSource dataSource, long fromId, long toId) {
        String sql = "DELETE FROM pizzas WHERE id BETWEEN ? AND ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, fromId);
            statement.setLong(2, toId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new DataAccessException("Не вдалося очистити тестові дані пакетної вставки", exception);
        }
    }
}
