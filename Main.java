import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            run(scanner);
        } catch (InputMismatchException e) {
            System.out.println("Помилка введення: перевірте тип введеного значення.");
        } catch (NoSuchElementException e) {
            System.out.println("Помилка введення: не отримано потрібне значення.");
        } catch (DomainException e) {
            System.out.printf("Помилка замовлення: %s. Некоректне значення: %s%n",
                    e.getMessage(), e.getInvalidValue());
        } finally {
            System.out.println("Завершення роботи програми.");
        }
    }

    private static void run(Scanner scanner) throws DomainException {
        System.out.print("Введіть кількість замовлень: ");
        int orderCount = scanner.nextInt();
        scanner.nextLine();
        if (orderCount <= 0) {
            throw new InvalidQuantityException(orderCount);
        }

        PizzaOrder[] orders = new PizzaOrder[orderCount];
        for (int i = 0; i < orders.length; i++) {
            System.out.printf("%nЗамовлення %d з %d%n", i + 1, orderCount);
            orders[i] = readOrder(scanner);
        }

        System.out.println("\nУсі замовлення:");
        for (PizzaOrder order : orders) {
            System.out.println(order);
        }

        System.out.print("\nПорахувати замовлення з ціною за піцу меншою за (грн): ");
        double priceLimit = scanner.nextDouble();
        int cheaperOrderCount = 0;
        for (PizzaOrder order : orders) {
            if (order.getUnitPrice() < priceLimit) {
                cheaperOrderCount++;
            }
        }
        System.out.printf("Кількість таких замовлень: %d%n", cheaperOrderCount);

        System.out.println("\nДо сортування за ціною піци:");
        printOrders(orders);
        bubbleSortByUnitPrice(orders);
        System.out.println("\nПісля сортування за ціною піци (за зростанням):");
        printOrders(orders);

        scanner.nextLine();
        System.out.println("\nВведіть усі поля замовлення для пошуку:");
        PizzaOrder sample = readOrder(scanner);
        PizzaOrder foundOrder = findOrder(orders, sample);
        if (foundOrder == null) {
            System.out.println("Замовлення з такими полями не знайдено.");
        } else {
            System.out.println("Знайдено: " + foundOrder);
        }

    }

    private static PizzaOrder readOrder(Scanner scanner) throws DomainException {
        System.out.print("Назва піци: ");
        String pizzaName = scanner.nextLine();
        System.out.print("Ціна за одиницю (грн): ");
        double unitPrice = scanner.nextDouble();
        System.out.print("Кількість (шт): ");
        int quantity = scanner.nextInt();
        System.out.print("Діаметр піци (см): ");
        int diameterCm = scanner.nextInt();
        System.out.print("Додатковий соус (true/false): ");
        boolean includesSauce = scanner.nextBoolean();
        scanner.nextLine();
        try {
            return new PizzaOrder(pizzaName, unitPrice, quantity, diameterCm, includesSauce);
        } catch (DomainException e) {
            System.out.println("Перевірка даних замовлення не пройдена; передаю помилку далі.");
            throw e;
        }
    }

    private static void printOrders(PizzaOrder[] orders) {
        for (PizzaOrder order : orders) {
            System.out.println(order);
        }
    }

    private static void bubbleSortByUnitPrice(PizzaOrder[] orders) {
        for (int i = 0; i < orders.length - 1; i++) {
            for (int j = 0; j < orders.length - 1 - i; j++) {
                if (orders[j].getUnitPrice() > orders[j + 1].getUnitPrice()) {
                    PizzaOrder temporary = orders[j];
                    orders[j] = orders[j + 1];
                    orders[j + 1] = temporary;
                }
            }
        }
    }

    private static PizzaOrder findOrder(PizzaOrder[] orders, PizzaOrder sample) {
        int index = 0;
        while (index < orders.length) {
            if (orders[index].equals(sample)) {
                return orders[index];
            }
            index++;
        }
        return null;
    }
}