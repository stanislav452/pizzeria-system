package ua.edu.pizzeria;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

/**
 * Демо лабораторної роботи №11: Hibernate ORM (Jakarta Persistence).
 *
 * Демонструє:
 *  - рівень 1: CRUD для Pizza через HibernatePizzaService (створення, читання, оновлення, видалення);
 *  - рівень 2: зв'язок "одна піця — багато замовлень" (@OneToMany / @ManyToOne),
 *              допоміжні методи addOrder/removeOrder, LAZY, cascade/orphanRemoval;
 *  - рівень 3: JPQL-запити з іменованими параметрами (сортування, пошук, пагінація),
 *              LazyInitializationException і його виправлення, N+1 і виправлення через JOIN FETCH.
 */
public class HibernateDemo {
    public static void main(String[] args) {
        // Ініціалізація схеми з порожньої БД (скрипт init.sql із ЛР №10, ідемпотентний).
        DatabaseInitializer.initialize();

        PizzaService pizzas = new HibernatePizzaService();
        OrderService orders = new HibernateOrderService();

        try {
            demonstrateCrud(pizzas);
            demonstrateRelationships(pizzas, orders);
            demonstrateJpql(pizzas, orders);
            demonstrateLazyProblem(orders);
            demonstrateNPlusOne(orders);
        } finally {
            JpaUtil.close();
        }
    }

    /* ========== Рівень 1: CRUD ========== */

    private static void demonstrateCrud(PizzaService pizzas) {
        System.out.println("\n=== Рівень 1. CRUD для Pizza через Hibernate ===");

        // CREATE
        Pizza pizza = pizzas.save(new Pizza("Тестова піца", 99.00, 30));
        System.out.println("Створено: " + pizza);

        // READ
        Pizza found = pizzas.findById(pizza.getId());
        System.out.println("Прочитано: " + found);

        // UPDATE
        found.setPrice(120.00);
        pizzas.save(found);
        System.out.println("Оновлено: " + pizzas.findById(pizza.getId()));

        // DELETE
        pizzas.deleteById(pizza.getId());
        System.out.println("Видалено id=" + pizza.getId());

        // EntityNotFoundException
        try {
            pizzas.findById(pizza.getId());
        } catch (EntityNotFoundException e) {
            System.out.println("Очікувано: " + e.getMessage());
        }
        try {
            pizzas.deleteById(99_999L);
        } catch (DataAccessException e) {
            System.out.println("Очікувано: " + e.getCause().getMessage());
        }
    }

    /* ========== Рівень 2: зв'язки ========== */

    private static void demonstrateRelationships(PizzaService pizzas, OrderService orders) {
        System.out.println("\n=== Рівень 2. Зв'язок Pizza <-> Order ===");

        Pizza margherita = ((HibernatePizzaService) pizzas).findByName("Маргарита").stream().findFirst().orElseThrow();
        System.out.println("Піца з меню: " + margherita);

        Order order1 = new Order(margherita, 3, true);
        Order order2 = new Order(margherita, 1, false);
        orders.save(order1);
        orders.save(order2);
        System.out.println("Створено замовлення id=" + order1.getId() + " та id=" + order2.getId());

        // Допоміжний метод узгоджує обидві сторони зв'язку
        margherita.addOrder(order1);
        System.out.println("Замовлень у піци через getOrders(): " + margherita.getOrders().size());

        // Пошук за назвою піци (JPQL, іменований параметр)
        List<Order> byPizza = ((HibernateOrderService) orders).findByPizzaName("Маргарита");
        System.out.println("Замовлення 'Маргарита' (JPQL): " + byPizza.size());

        // Видалення піци cascade-но витягає залежні замовлення (orphanRemoval)
        Pizza temp = pizzas.save(new Pizza("Тимчасова піца", 50.00, 25));
        orders.save(new Order(temp, 2, false));
        Pizza tempWithOrders = JpaUtil.readOnly(em -> em.createQuery(
                "SELECT p FROM Pizza p JOIN FETCH p.orders WHERE p.id = :id", Pizza.class)
                .setParameter("id", temp.getId()).getSingleResult());
        System.out.println("Перед видаленням замовлень тимчасової піци: " + tempWithOrders.getOrders().size());
        pizzas.deleteById(temp.getId());
        System.out.println("Після видалення піци замовлення вилучено cascade/orphanRemoval.");
    }

    /* ========== Рівень 3: JPQL ========== */

    private static void demonstrateJpql(PizzaService pizzas, OrderService orders) {
        System.out.println("\n=== Рівень 3. JPQL-запити ===");

        System.out.println("Піци за назвою та ціною (ORDER BY):");
        for (Pizza p : pizzas.findAllSortedByNameAndByPrice()) {
            System.out.println("  " + p);
        }

        System.out.println("Пошук піци за назвою (іменований параметр):");
        for (Pizza p : ((HibernatePizzaService) pizzas).findByName("Маргарита")) {
            System.out.println("  " + p);
        }

        System.out.println("Посторінкова вибірка піц (offset=0, limit=2):");
        for (Pizza p : ((HibernatePizzaService) pizzas).findPage(0, 2)) {
            System.out.println("  " + p);
        }

        System.out.println("Замовлення за назвою й кількістю (ORDER BY):");
        for (Order o : orders.findAllSortedByNameAndByQuantity()) {
            System.out.println("  " + o);
        }

        System.out.println("Посторінкова вибірка замовлень (offset=0, limit=5):");
        for (Order o : ((HibernateOrderService) orders).findPage(0, 5)) {
            System.out.println("  " + o);
        }
    }

    /* ========== LazyInitializationException ========== */

    private static void demonstrateLazyProblem(OrderService orders) {
        System.out.println("\n=== Проблема: LazyInitializationException ===");

        Order order = orders.findById(1L); // pizza завантажується ліниво (LAZY)
        System.out.println("Замовлення завантажено: " + order);

        try {
            // EntityManager уже закритий — доступ до лінивого проксі викликає виняток
            System.out.println("Піца через проксі: " + order.getPizza().getName());
        } catch (org.hibernate.LazyInitializationException e) {
            System.out.println("Очікувано: " + e.getMessage());
        }

        // Виправлення: працювати зі зв'язком всередині відкритого EntityManager
        Pizza pizza = JpaUtil.inTransaction(em -> {
            Order loaded = em.find(Order.class, order.getId());
            return loaded.getPizza();
        });
        System.out.println("Виправлено: піца = " + pizza.getName());
    }

    /* ========== N+1 ========== */

    private static void demonstrateNPlusOne(OrderService orders) {
        System.out.println("\n=== Проблема N+1 ===");

        List<Order> all = orders.findAll(); // 1 запит на замовлення
        System.out.println("Завантажено замовлень: " + all.size());

        // ЧИСТИЙ варіант: доступ до pizza.allOrder в циклі → ще N запитів (N+1 загалом)
        System.out.println("Через ліниві проксі (N+1):");
        for (Order o : all) {
            System.out.println("  замовлення id=" + o.getId() + " → піца " + o.getPizza().getName());
        }

        // Виправлення: JOIN FETCH завантажує пов'язані об'єкти одним запитом
        List<Order> withPizza = JpaUtil.readOnly(em -> em.createQuery(
                "SELECT o FROM Order o JOIN FETCH o.pizza", Order.class)
                .getResultList());
        System.out.println("З JOIN FETCH (1 запит):");
        for (Order o : withPizza) {
            System.out.println("  замовлення id=" + o.getId() + " → піца " + o.getPizza().getName());
        }
    }
}
