import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Введіть назву піци: ");
        String pizzaName = scanner.nextLine();

        System.out.print("Введіть ціну за одиницю (грн): ");
        double unitPrice = scanner.nextDouble();

        System.out.print("Введіть кількість (шт): ");
        int quantity = scanner.nextInt();

        System.out.print("Введіть діаметр піци (см): ");
        int diameterCm = scanner.nextInt();

        System.out.print("Чи потрібен додатковий соус? (true/false): ");
        boolean includesSauce = scanner.nextBoolean();

        double baseTotal = unitPrice * quantity;
        if (includesSauce) {
            baseTotal += 25.0 * quantity;
        }

        double discountRate = 0.0;
        if (quantity >= 3 || baseTotal >= 600.0) {
            discountRate = 0.15;
        } else if (quantity == 2) {
            discountRate = 0.05;
        }

        double discountAmount = baseTotal * discountRate;
        double finalTotal = baseTotal - discountAmount;

        System.out.println("\n==========================================");
        System.out.println("           ЕЛЕКТРОННЕ МЕНЮ ПІЦЕРІЇ        ");
        System.out.println("==========================================");
        System.out.printf("Позиція:            %s (%d см)%n", pizzaName, diameterCm);
        System.out.printf("Ціна за шт:         %.2f грн%n", unitPrice);
        System.out.printf("Кількість:          %d шт%n", quantity);
        System.out.printf("Додатковий соус:    %s%n", includesSauce ? "Так (+25 грн/шт)" : "Ні");
        System.out.println("------------------------------------------");
        System.out.printf("Сума без знижки:    %.2f грн%n", baseTotal);
        System.out.printf("Знижка (%d%%):        -%.2f грн%n", (int)(discountRate * 100), discountAmount);
        System.out.printf("РАЗОМ ДО СПЛАТИ:    %.2f грн%n", finalTotal);
        System.out.println("==========================================");

        scanner.close();
    }
}