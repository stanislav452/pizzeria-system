package ua.edu.pizzeria;

/**
 * Неперевірюваний (unchecked) виняток для помилок доступу до БД.
 * JDBC кидає checked {@link java.sql.SQLException}, а методи CRUDService
 * не оголошують checked-винятків, тому SQLException обгортається цим класом.
 */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
