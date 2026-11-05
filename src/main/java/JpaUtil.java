package ua.edu.pizzeria;

import java.util.function.Consumer;
import java.util.function.Function;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

/**
 * Singleton для роботи з Hibernate ORM (Jakarta Persistence).
 *
 * <ul>
 *   <li>{@link EntityManagerFactory} створюється один раз під час завантаження класу.</li>
 *   <li>{@link EntityManager} створюється на кожну операцію й закривається після неї.</li>
 *   <li>{@link #inTransaction} виконує дію в межах однієї транзакції з commit/rollback.</li>
 *   <li>{@link #readOnly} виконує читання без транзакції.</li>
 * </ul>
 */
public final class JpaUtil {
    private static final EntityManagerFactory ENTITY_MANAGER_FACTORY =
            Persistence.createEntityManagerFactory("pizzeria-pu");

    private JpaUtil() {
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        return ENTITY_MANAGER_FACTORY;
    }

    /** Створює новий EntityManager. Викликач повинно закрити його через {@link EntityManager#close()}. */
    public static EntityManager createEntityManager() {
        return ENTITY_MANAGER_FACTORY.createEntityManager();
    }

    /** Залишаємо закритий стан фабрики при завершенні застосунку. */
    public static void close() {
        if (ENTITY_MANAGER_FACTORY != null && ENTITY_MANAGER_FACTORY.isOpen()) {
            ENTITY_MANAGER_FACTORY.close();
        }
    }

    /**
     * Виконує {@code action} в транзакції: {@code begin} → дія → {@code commit};
     * при помилці — {@code rollback}. EntityManager закривається у будь-якому разі.
     */
    public static <T> T inTransaction(Function<EntityManager, T> action) {
        EntityManager em = createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T result = action.apply(em);
            tx.commit();
            return result;
        } catch (Exception exception) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new DataAccessException("Транзакцію відмінено через помилку", exception);
        } finally {
            em.close();
        }
    }

    public static void inTransaction(Consumer<EntityManager> action) {
        inTransaction(em -> {
            action.accept(em);
            return null;
        });
    }

    /**
     * Виконує читну дію без транзакції. EntityManager закривається після виходу.
     * Увага: лічені зв'язки (LAZY) будуть недоступні після закриття EntityManager.
     */
    public static <T> T readOnly(Function<EntityManager, T> action) {
        EntityManager em = createEntityManager();
        try {
            return action.apply(em);
        } finally {
            em.close();
        }
    }
}
