import java.util.Collection;

public class Main {
    public static void printAll(Collection<? extends BaseEntity> items) {
        for (BaseEntity item : items) {
            System.out.println(item);
        }
    }

    public static void main(String[] args) {
        PizzaService pizzas = new InMemoryPizzaService();
        pizzas.save(new Pizza(1, "Маргарита", 140.0, 32));
        pizzas.save(new Pizza(2, "Пепероні", 180.0, 34));
        pizzas.save(new Pizza(3, "Маргарита", 160.0, 28));
        pizzas.save(new Pizza(4, "Чотири сири", 210.0, 36));

        System.out.println("Піци до сортування:");
        printAll(pizzas.findAll());
        System.out.println("\nПіци за назвою:");
        printAll(pizzas.findAllSortedByName());
        System.out.println("\nПіци за назвою та ціною:");
        printAll(pizzas.findAllSortedByNameAndByPrice());

        int countBeforeUpdate = pizzas.findAll().size();
        pizzas.save(new Pizza(1, "Маргарита", 150.0, 32));
        System.out.println("\nОновлена піца: " + pizzas.findById(1));
        System.out.println("Кількість до і після оновлення: " + countBeforeUpdate
                + " і " + pizzas.findAll().size());

        pizzas.deleteById(4);
        System.out.println("Після успішного видалення піци з id=4:");
        printAll(pizzas.findAll());

        try {
            pizzas.deleteById(99);
        } catch (EntityNotFoundException exception) {
            System.out.println("Неуспішне видалення: " + exception.getMessage());
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

        System.out.println("\nЗамовлення до сортування:");
        printAll(orders.findAll());
        System.out.println("Замовлення за назвою та кількістю піц:");
        printAll(orders.findAllSortedByNameAndByQuantity());
    }
}
