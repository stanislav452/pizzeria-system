package ua.edu.pizzeria;

import java.util.Collection;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    public static void printAll(Collection<? extends BaseEntity> items) {
        for (BaseEntity item : items) System.out.println(item);
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            run(scanner);
        } catch (InputMismatchException e) {
            System.out.println("Помилка введення: перевірте тип введеного значення.");
        } catch (NoSuchElementException e) {
            System.out.println("Помилка введення: не отримано потрібне значення.");
        } catch (DomainException e) {
            System.out.printf("Помилка замовлення: %s. Некоректне значення: %s%n", e.getMessage(), e.getInvalidValue());
        } catch (IllegalArgumentException e) {
            System.out.printf("Помилка даних замовлення: %s%n", e.getMessage());
        } finally {
            System.out.println("Завершення роботи програми.");
        }
    }

    private static void run(Scanner scanner) {
        System.out.print("Введіть кількість замовлень: ");
        int orderCount = scanner.nextInt();
        scanner.nextLine();
        if (orderCount <= 0) throw new InvalidQuantityException(orderCount);

        PizzaOrder[] orders = new PizzaOrder[orderCount];
        for (int i = 0; i < orders.length; i++) {
            System.out.printf("%nЗамовлення %d з %d%n", i + 1, orderCount);
            orders[i] = readOrder(scanner);
        }

        System.out.println("\nУсі замовлення:");
        for (PizzaOrder order : orders) System.out.println(order);
        demonstrateOrderHierarchy(orders);
        demonstratePricingStrategies(orders);
        demonstrateOrderStatusObservers(orders[0]);

        System.out.print("\nПорахувати замовлення з ціною за піцу меншою за (грн): ");
        double priceLimit = scanner.nextDouble();
        int cheaperOrderCount = 0;
        for (PizzaOrder order : orders) if (order.getUnitPrice() < priceLimit) cheaperOrderCount++;
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
        System.out.println(foundOrder == null ? "Замовлення з такими полями не знайдено." : "Знайдено: " + foundOrder);

        demonstrateCrudService();
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
            System.out.println("Тип отримання: 1 — самовивіз, 2 — доставка, 3 — експрес-доставка");
            int fulfillmentType = scanner.nextInt();
            scanner.nextLine();
            return switch (fulfillmentType) {
                case 1 -> new PickupOrder(pizzaName, unitPrice, quantity, diameterCm, includesSauce);
                case 2, 3 -> {
                    System.out.print("Адреса доставки: ");
                    String address = scanner.nextLine();
                    System.out.print("Вартість доставки (грн): ");
                    double deliveryFee = scanner.nextDouble();
                    scanner.nextLine();
                    if (fulfillmentType == 2) yield new DeliveryOrder(pizzaName, unitPrice, quantity, diameterCm, includesSauce, address, deliveryFee);
                    System.out.print("Експрес-доплата (грн): ");
                    double expressFee = scanner.nextDouble();
                    scanner.nextLine();
                    yield new ExpressDeliveryOrder(pizzaName, unitPrice, quantity, diameterCm, includesSauce, address, deliveryFee, expressFee);
                }
                default -> throw new IllegalArgumentException("Невідомий тип отримання: " + fulfillmentType);
            };
        } catch (DomainException e) {
            System.out.println("Перевірка даних замовлення не пройдена; передаю помилку далі.");
            throw e;
        }
    }

    private static void demonstrateOrderHierarchy(PizzaOrder[] orders) {
        System.out.println("\nПоліморфний опис типу отримання:");
        for (PizzaOrder order : orders) {
            System.out.println(describeFulfillment(order));
            if (order instanceof DeliveryOrder deliveryOrder) System.out.println("Специфічні дані доставки: " + deliveryOrder.getDeliveryAddress());
        }
        if (orders.length > 1) System.out.printf("Рівність у двох напрямках (за id): %s / %s%n", orders[0].equals(orders[1]), orders[1].equals(orders[0]));
    }

    private static String describeFulfillment(PizzaOrder order) {
        return switch (order) {
            case PickupOrder pickup -> pickup.getFulfillmentDescription();
            case ExpressDeliveryOrder express -> express.getFulfillmentDescription();
            case DeliveryOrder delivery -> delivery.getFulfillmentDescription();
        };
    }

    private static void printOrders(PizzaOrder[] orders) {
        for (PizzaOrder order : orders) System.out.println(order);
    }

    private static void demonstratePricingStrategies(PizzaOrder[] orders) {
        OrderPricingStrategy[] strategies = {
                OrderPricingStrategyFactory.create(OrderPricingStrategyFactory.STANDARD),
                OrderPricingStrategyFactory.create(OrderPricingStrategyFactory.HAPPY_HOUR)
        };
        System.out.println("\nРозрахунок замовлень різними стратегіями:");
        for (PizzaOrder order : orders) {
            System.out.println(order);
            for (OrderPricingStrategy strategy : strategies) {
                System.out.printf("%s: %.2f грн%n", strategy.getName(), strategy.calculateTotal(order));
                if (strategy instanceof PromotionDescription promotion) System.out.println(promotion.getPromotionDescription());
            }
        }
        OrderCheckout checkout = new OrderCheckout(strategies[0]);
        System.out.printf("%nЗміна стратегії під час роботи програми (%s): %.2f грн%n", checkout.getStrategyName(), checkout.calculateTotal(orders[0]));
        checkout.setPricingStrategy(strategies[1]);
        System.out.printf("Після зміни стратегії (%s): %.2f грн%n", checkout.getStrategyName(), checkout.calculateTotal(orders[0]));
    }

    private static void demonstrateOrderStatusObservers(PizzaOrder order) {
        order.addStatusListener(new OrderStatusLogger());
        order.addStatusListener(new KitchenDisplayAdapter(new LegacyKitchenDisplay()));
        System.out.println("\nДемонстрація сповіщення про зміну статусу:");
        order.setStatus(OrderStatus.PREPARING);
        order.setStatus(OrderStatus.READY);
    }

    private static void bubbleSortByUnitPrice(PizzaOrder[] orders) {
        for (int i = 0; i < orders.length - 1; i++) {
            for (int j = 0; j < orders.length - 1 - i; j++) {
                if (orders[j].getUnitPrice() > orders[j + 1].getUnitPrice()) {
                    PizzaOrder temporary = orders[j]; orders[j] = orders[j + 1]; orders[j + 1] = temporary;
                }
            }
        }
    }

    private static PizzaOrder findOrder(PizzaOrder[] orders, PizzaOrder sample) {
        for (PizzaOrder order : orders) {
            if (order.getClass() == sample.getClass()
                    && order.getPizzaName().equals(sample.getPizzaName())
                    && Double.compare(order.getUnitPrice(), sample.getUnitPrice()) == 0
                    && order.getQuantity() == sample.getQuantity()
                    && order.getDiameterCm() == sample.getDiameterCm()
                    && order.includesSauce() == sample.includesSauce()
                    && order.getFulfillmentDescription().equals(sample.getFulfillmentDescription())) return order;
        }
        return null;
    }

    private static void demonstrateCrudService() {
        System.out.println("\n=== CRUD лабораторна №7–9 ===");
        PizzaOrderService service = new InMemoryPizzaOrderService();
        service.save(new PickupOrder(90001, "Маргарита", 140, 1, 32, false, "Головна піцерія"));
        service.save(new PickupOrder(90002, "Пепероні", 180, 1, 34, false, "Головна піцерія"));
        service.save(new PickupOrder(90003, "Маргарита", 160, 1, 28, false, "Головна піцерія"));

        System.out.println("Замовлення до сортування:");
        printAll(service.findAll());
        System.out.println("Замовлення за назвою піци та ціною:");
        printAll(service.findAllSortedByNameAndByPrice());

        int countBefore = service.findAll().size();
        service.save(new PickupOrder(90001, "Маргарита", 150, 1, 32, false, "Головна піцерія"));
        System.out.printf("Оновлення id=90001; кількість до/після: %d/%d%n", countBefore, service.findAll().size());
        service.deleteById(90003);
        System.out.println("Після видалення id=90003:");
        printAll(service.findAll());
        try { service.deleteById(99999); }
        catch (EntityNotFoundException exception) { System.out.println("Видалення: " + exception.getMessage()); }
        try { service.findById(99999); }
        catch (EntityNotFoundException exception) { System.out.println("Пошук: " + exception.getMessage()); }
    }
}
