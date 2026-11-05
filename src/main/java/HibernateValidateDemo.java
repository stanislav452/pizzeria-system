package ua.edu.pizzeria;

/**
 * Демонстрація помилки валідації сховища Hibernate.
 *
 * Створює EntityManagerFactory з сутністю {@link PizzaMismatch}, у якій
 * {@code price} має scale = 3, тоді як у таблиці {@code pizzas} стовпець
 * {@code price} оголошений як NUMERIC(10, 2) (scale = 2).
 * При {@code hibernate.hbm2ddl.auto = validate} Hibernate виявляє розбіжність
 * і кидає виняток ще на етапі створення фабрики.
 */
public class HibernateValidateDemo {
    public static void main(String[] args) {
        var registryBuilder = new org.hibernate.boot.registry.StandardServiceRegistryBuilder();
        registryBuilder
                .applySetting("hibernate.connection.url", "jdbc:h2:file:./data/pizzeria")
                .applySetting("hibernate.connection.user", "sa")
                .applySetting("hibernate.connection.password", "")
                .applySetting("hibernate.connection.driver_class", "org.h2.Driver")
                .applySetting("hibernate.dialect", "org.hibernate.dialect.H2Dialect")
                .applySetting("hibernate.hbm2ddl.auto", "validate")
                .applySetting("hibernate.show_sql", "false");

        var registry = registryBuilder.build();
        try {
            new org.hibernate.boot.MetadataSources(registry)
                    .addAnnotatedClass(PizzaMismatch.class)
                    .buildMetadata()
                    .buildSessionFactory();
            System.out.println("Помилка валідації не виникла (очікувалося виявлення розбіжності).");
        } catch (Exception e) {
            System.out.println("Очікувана помилка Hibernate при validate:");
            System.out.println("  " + e.getClass().getName());
            Throwable cause = e.getCause();
            System.out.println("  приčina: " + (cause != null ? cause.getMessage() : e.getMessage()));
        } finally {
            org.hibernate.boot.registry.StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
