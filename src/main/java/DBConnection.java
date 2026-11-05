package ua.edu.pizzeria;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

/**
 * Singleton, який тримає не одне {@link Connection}, а пул з'єднань HikariCP.
 *
 * <p>Параметри підключення читаються з {@code src/main/resources/application.properties},
 * тому URL, користувач і пароль не зашиті в код. Будь-який параметр можна
 * перевизначити системною властивістю, наприклад
 * {@code -Ddb.url=jdbc:h2:file:./data/other}.</p>
 *
 * <p>Пул потокобезпечний: {@link #getConnection()} видає кожному виклику окреме
 * з'єднання, а після {@code close()} повертає його в пул.</p>
 */
public final class DBConnection {
    private static final String PROPERTIES_RESOURCE = "application.properties";

    private static final DBConnection INSTANCE = new DBConnection();

    private final HikariDataSource dataSource;

    private DBConnection() {
        Properties properties = loadProperties();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(required(properties, "db.url"));
        config.setUsername(properties.getProperty("db.username", ""));
        config.setPassword(properties.getProperty("db.password", ""));

        String driver = properties.getProperty("db.driver", "").trim();
        if (!driver.isEmpty()) config.setDriverClassName(driver);

        config.setMaximumPoolSize(intProperty(properties, "db.pool.maximumPoolSize", 10));
        config.setMinimumIdle(intProperty(properties, "db.pool.minimumIdle", 2));
        config.setConnectionTimeout(intProperty(properties, "db.pool.connectionTimeoutMs", 5_000));
        config.setPoolName(properties.getProperty("db.pool.poolName", "pizzeria-pool"));
        config.setAutoCommit(true);

        this.dataSource = new HikariDataSource(config);
    }

    public static DBConnection getInstance() {
        return INSTANCE;
    }

    /** @return з'єднання з пулу; його потрібно закривати через try-with-resources. */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    /** Закриває пул. Викликається один раз після завершення демонстрації. */
    public void close() {
        dataSource.close();
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = DBConnection.class.getClassLoader()
                .getResourceAsStream(PROPERTIES_RESOURCE)) {
            if (input == null) {
                throw new IllegalStateException(
                        "Не знайдено " + PROPERTIES_RESOURCE + " у classpath (src/main/resources)");
            }
            properties.load(input);
            return properties;
        } catch (IOException exception) {
            throw new IllegalStateException("Не вдалося прочитати " + PROPERTIES_RESOURCE, exception);
        }
    }

    private static String required(Properties properties, String key) {
        String value = System.getProperty(key, properties.getProperty(key));
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("У application.properties не задано " + key);
        }
        return Objects.requireNonNull(value);
    }

    private static int intProperty(Properties properties, String key, int defaultValue) {
        String value = System.getProperty(key, properties.getProperty(key));
        return value == null || value.isBlank() ? defaultValue : Integer.parseInt(value.trim());
    }
}
