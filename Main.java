import java.util.Objects;
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

class PizzaOrder {
    private final String pizzaName;
    private final double unitPrice;
    private final int quantity;
    private final int diameterCm;
    private final boolean includesSauce;

    PizzaOrder(String pizzaName, double unitPrice, int quantity, int diameterCm, boolean includesSauce)
            throws DomainException {
        if (!Double.isFinite(unitPrice) || unitPrice <= 0) {
            throw new InvalidPriceException(unitPrice);
        }
        if (quantity <= 0) {
            throw new InvalidQuantityException(quantity);
        }
        this.pizzaName = pizzaName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.diameterCm = diameterCm;
        this.includesSauce = includesSauce;
    }

    double getUnitPrice() {
        return unitPrice;
    }

    @Override
    public String toString() {
        double total = (unitPrice + (includesSauce ? 25.0 : 0.0)) * quantity;
        double discountRate = 0.0;
        if (quantity >= 3 || total >= 600.0) {
            discountRate = 0.15;
        } else if (quantity == 2) {
            discountRate = 0.05;
        }
        return String.format(
                "%s (%d см), ціна: %.2f грн/шт, кількість: %d, соус: %s, сума: %.2f грн",
                pizzaName, diameterCm, unitPrice, quantity, includesSauce ? "так" : "ні",
                total * (1.0 - discountRate));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PizzaOrder)) {
            return false;
        }
        PizzaOrder order = (PizzaOrder) other;
        return Double.compare(unitPrice, order.unitPrice) == 0
                && quantity == order.quantity
                && diameterCm == order.diameterCm
                && includesSauce == order.includesSauce
                && Objects.equals(pizzaName, order.pizzaName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pizzaName, unitPrice, quantity, diameterCm, includesSauce);
    }
}

class DomainException extends Exception {
    private final String invalidValue;

    DomainException(String message, String invalidValue) {
        super(message);
        this.invalidValue = invalidValue;
    }

    String getInvalidValue() {
        return invalidValue;
    }
}

class InvalidPriceException extends DomainException {
    InvalidPriceException(double price) {
        super("ціна піци має бути додатною та скінченною", String.valueOf(price));
    }
}

class InvalidQuantityException extends DomainException {
    InvalidQuantityException(int quantity) {
        super("кількість має бути більшою за нуль", String.valueOf(quantity));
    }
}