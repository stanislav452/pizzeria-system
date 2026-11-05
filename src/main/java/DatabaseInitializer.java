package ua.edu.pizzeria;

import org.h2.tools.RunScript;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Виконує {@code init.sql} з classpath при старті застосунку.
 * Скрипт ідемпотентний (CREATE TABLE IF NOT EXISTS, MERGE ... KEY), тому
 * повторний запуск не падає і не дублює стартові дані.
 */
public final class DatabaseInitializer {
    private static final String INIT_SCRIPT = "init.sql";

    private DatabaseInitializer() {
    }

    public static void initialize() {
        try (Connection connection = DBConnection.getInstance().getConnection();
             InputStream input = DatabaseInitializer.class.getClassLoader()
                     .getResourceAsStream(INIT_SCRIPT)) {

            if (input == null) {
                throw new IllegalStateException(
                        "Не знайдено " + INIT_SCRIPT + " у classpath (src/main/resources)");
            }

            try (Reader reader = new InputStreamReader(
                    Objects.requireNonNull(input), StandardCharsets.UTF_8)) {
                RunScript.execute(connection, reader);
            }
            System.out.println("Ініціалізацію схеми БД виконано (" + INIT_SCRIPT + ").");
        } catch (SQLException | IOException exception) {
            throw new DataAccessException("Не вдалося виконати " + INIT_SCRIPT, exception);
        }
    }
}
