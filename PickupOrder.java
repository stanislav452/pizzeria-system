import java.util.Objects;

public final class PickupOrder extends PizzaOrder {
    private final String pickupPoint;

    public PickupOrder(String pizzaName, double unitPrice, int quantity, int diameterCm,
                       boolean includesSauce) {
        this(pizzaName, unitPrice, quantity, diameterCm, includesSauce, "Головна піцерія");
        System.out.println("Конструктор PickupOrder через this(...)");
    }

    public PickupOrder(String pizzaName, double unitPrice, int quantity, int diameterCm,
                       boolean includesSauce, String pickupPoint) {
        super(pizzaName, unitPrice, quantity, diameterCm, includesSauce);
        this.pickupPoint = Objects.requireNonNull(pickupPoint, "Пункт самовивозу не може бути null");
        System.out.println("Конструктор PickupOrder через super(...)");
    }

    public String getPickupPoint() {
        return pickupPoint;
    }

    @Override
    public String getFulfillmentDescription() {
        return "самовивіз із пункту «" + pickupPoint + "»";
    }

    @Override
    public double getFulfillmentFee() {
        return 0.0;
    }

    @Override
    public String toString() {
        return super.toString() + ", " + getFulfillmentDescription();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PickupOrder order)) {
            return false;
        }
        return super.equals(other) && Objects.equals(pickupPoint, order.pickupPoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), pickupPoint);
    }
}
