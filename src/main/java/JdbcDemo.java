package ua.edu.pizzeria;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * Демонстрація лабораторної роботи №10:
 * Maven, JDBC, транзакції, SQL-ін'єкції, пул з'єднань, batch і пагінація.
 *
 * <p>Це окрема точка входу. Клас {@code Main} з попередніх лабораторних
 * залишається без змін; запустити його можна командою
 * {@code mvn exec:java -Dexec.mainClass=ua.edu.pizzeria.Main}.</p>
 */
public class JdbcDemo {
    private static final int BATCH_ROW_COUNT = 1_000;

    public static void main(String[] args) {
        DatabaseInitializer.initialize();
        DataSource dataSource = DBConnection.getInstance().getDataSource();

        try {
            cleanupDemoData(dataSource);

            JdbcPizzaService pizzas = new JdbcPizzaService(dataSource);
            JdbcOrderService orders = new JdbcOrderService(dataSource);

            demonstrateServiceSwap(pizzas);
            demonstratePizzaCrud(pizzas);
            demonstrateRelatedEntity(orders);
            demonstrateTransaction(dataSource, pizzas);
            demonstrateSqlInjection(pizzas);
            demonstrateBatch(dataSource);
            demonstratePagination(pizzas);
        } finally {
            DBConnection.getInstance().close();
        }
    }

    private static void demonstrateServiceSwap(JdbcPizzaService jdbcPizzas) {
        System.out.println("\n=== Спільний інтерфейс: in-memory та JDBC ===");

        PizzaService inMemory = new InMemoryPizzaService();
        inMemory.save(new Pizza(1, "Маргарита", 140, 32));
        inMemory.save(new Pizza(2, "Пепероні", 180, 34));
        inMemory.save(new Pizza(3, "Маргарита", 160, 28));

        System.out.println("InMemoryPizzaService (без БД):");
        printCatalogue(inMemory);
        System.out.println("JdbcPizzaService (та сама сигнатура методів, інша реалізація):");
        printCatalogue(jdbcPizzas);
    }

    /** Клієнтський код не змінюється: йому достатньо посилання на інтерфейс. */
    private static void printCatalogue(PizzaService service) {
        for (Pizza pizza : service.findAllSortedByNameAndByPrice()) {
            System.out.println("  " + pizza);
        }
    }

    private static void demonstratePizzaCrud(JdbcPizzaService pizzas) {
        System.out.println("\n=== Рівень 1. CRUD для Pizza через JDBC ===");

        pizzas.save(new Pizza(1001, "Демо Маргарита", 145.50, 32));
        pizzas.save(new Pizza(1002, "Демо Пепероні", 185.00, 34));
        pizzas.save(new Pizza(1003, "Демо Гавайська", 195.00, 30));

        System.out.println("Після додавання трьох піц:");
        printAll(pizzas.findAll());

        System.out.println("Сортування через ORDER BY name, price:");
        printAll(pizzas.findAllSortedByNameAndByPrice());

        pizzas.save(new Pizza(1001, "Демо Маргарита", 150.00, 32));
        System.out.println("Після оновлення id=1001: " + pizzas.findById(1001));

        pizzas.deleteById(1003);
        System.out.println("Після видалення id=1003:");
        printAll(pizzas.findAll());

        try {
            pizzas.findById(1003);
        } catch (EntityNotFoundException exception) {
            System.out.println("Очікувано: " + exception.getMessage());
        }
    }

    private static void demonstrateRelatedEntity(JdbcOrderService orders) {
        System.out.println("\n=== Рівень 2. Друга сутність Order (FOREIGN KEY -> pizzas.name) ===");

        orders.save(new Order(2001, "Демо Маргарита", 3, true));
        orders.save(new Order(2002, "Демо Пепероні", 1, false));
        orders.save(new Order(2003, "Демо Маргарита", 2, false));

        System.out.println("Замовлення за назвою піци та кількістю (ORDER BY name, quantity):");
        printAll(orders.findAllSortedByNameAndByQuantity());
    }

    private static void demonstrateTransaction(DataSource dataSource, JdbcPizzaService pizzas) {
        System.out.println("\n=== Рівень 2. Транзакція на дві таблиці ===");
        JdbcOrderPlacementService placement = new JdbcOrderPlacementService(dataSource);

        placement.createPizzaWithOrder(
                new Pizza(1101, "Демо Транзакція", 155.00, 30),
                new Order(2101, "Демо Транзакція", 2, false));
        System.out.println("Успішний commit: піца id=1101 та замовлення id=2101 збережені.");
        System.out.println("  " + pizzas.findById(1101));

        System.out.println("Тепер навмисно порушуємо FOREIGN KEY у другій вставці...");
        try {
            placement.createPizzaWithOrder(
                    new Pizza(1102, "Демо Відкат", 165.00, 31),
                    new Order(2102, "Піца, якої немає в меню", 1, false));
        } catch (DataAccessException exception) {
            System.out.println("Очікувана помилка: " + exception.getMessage());
        }

        try {
            pizzas.findById(1102);
            System.out.println("ПОМИЛКА: піца id=1102 мала зникнути після rollback!");
        } catch (EntityNotFoundException exception) {
            System.out.println("Rollback підтверджено: " + exception.getMessage());
        }
    }

    private static void demonstrateSqlInjection(JdbcPizzaService pizzas) {
        System.out.println("\n=== Рівень 2. SQL-ін'єкція та PreparedStatement ===");

        List<Pizza> normalSearch = pizzas.findByName("Маргарита");
        System.out.println("Звичайний пошук 'Маргарита': знайдено " + normalSearch.size());

        String attack = "' OR '1'='1";
        List<Pizza> attackResult = pizzas.findByName(attack);
        System.out.println("Спроба ін'єкції findByName(\"" + attack + "\"): знайдено "
                + attackResult.size() + " (рядок сприйнято як дані, а не як SQL)");
    }

    private static void demonstrateBatch(DataSource dataSource) {
        System.out.println("\n=== Рівень 3. Пакетна вставка addBatch/executeBatch ===");
        JdbcBatchDemo.run(dataSource, BATCH_ROW_COUNT);
    }

    private static void demonstratePagination(JdbcPizzaService pizzas) {
        System.out.println("\n=== Рівень 3. Посторінкове читання LIMIT/OFFSET ===");
        System.out.println("Сторінка 1 (offset=0, limit=3):");
        printAll(pizzas.findPage(0, 3));
        System.out.println("Сторінка 2 (offset=3, limit=3):");
        printAll(pizzas.findPage(3, 3));
        System.out.println("findAll() без обмежень вивантажує всю таблицю в пам'ять — "
                + "на великих обсягах це небезпечно.");
    }

    /** Прибирає лише демонстраційні записи, щоб повторний запуск був ідемпотентним. */
    private static void cleanupDemoData(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            deleteRange(connection, "DELETE FROM orders WHERE id BETWEEN ? AND ?", 2_000, 2_999);
            deleteRange(connection, "DELETE FROM pizzas WHERE id BETWEEN ? AND ?", 1_000, 1_999);
            deleteRange(connection, "DELETE FROM pizzas WHERE id BETWEEN ? AND ?", 100_000, 300_000);
        } catch (SQLException exception) {
            throw new DataAccessException("Не вдалося очистити демонстраційні дані", exception);
        }
    }

    private static void deleteRange(Connection connection, String sql, long fromId, long toId)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, fromId);
            statement.setLong(2, toId);
            statement.executeUpdate();
        }
    }

    private static void printAll(Collection<? extends BaseEntity> items) {
        for (BaseEntity item : items) System.out.println("  " + item);
    }
}
