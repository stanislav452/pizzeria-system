package ua.edu.pizzeria;

import java.util.Collection;

public class CrudDemo {
    public static void printAll(Collection<? extends BaseEntity> items) {
        for (BaseEntity item : items) System.out.println(item);
    }

    public static void main(String[] args) {
        PizzaService pizzas = new InMemoryPizzaService();
        pizzas.save(new Pizza(1, "Маргарита", 140, 32));
        pizzas.save(new Pizza(2, "Пепероні", 180, 34));
        pizzas.save(new Pizza(3, "Маргарита", 160, 28));

        System.out.println("Піци до сортування:");
        printAll(pizzas.findAll());
        System.out.println("\nПіци за назвою та ціною:");
        printAll(pizzas.findAllSortedByNameAndByPrice());

        int before = pizzas.findAll().size();
        pizzas.save(new Pizza(1, "Маргарита", 150, 32));
        System.out.println("\nОновлено: " + pizzas.findById(1));
        System.out.println("Кількість до/після оновлення: " + before + "/" + pizzas.findAll().size());

        pizzas.deleteById(2);
        System.out.println("\nПісля видалення id=2:");
        printAll(pizzas.findAll());

        try {
            pizzas.deleteById(99);
        } catch (EntityNotFoundException exception) {
            System.out.println("Видалення: " + exception.getMessage());
        }
        try {
            pizzas.findById(99);
        } catch (EntityNotFoundException exception) {
            System.out.println("Пошук: " + exception.getMessage());
        }

        OrderService orders = new InMemoryOrderService();
        orders.save(new Order(10, "Замовлення", 3, true));
        orders.save(new Order(11, "Самовивіз", 2, false));
        orders.save(new Order(12, "Замовлення", 1, false));
        System.out.println("\nЗамовлення за назвою та кількістю:");
        printAll(orders.findAllSortedByNameAndByQuantity());
    }
}
