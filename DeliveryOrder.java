import java.util.Objects;

public sealed class DeliveryOrder extends PizzaOrder permits ExpressDeliveryOrder {
    private final DeliveryDetails deliveryDetails;

    public DeliveryOrder(String pizzaName, double unitPrice, int quantity, int diameterCm,
                         boolean includesSauce, String address, double deliveryFee) {
        this(pizzaName, unitPrice, quantity, diameterCm, includesSauce,
                new DeliveryDetails(address, deliveryFee));
    }

    public DeliveryOrder(String pizzaName, double unitPrice, int quantity, int diameterCm,
                         boolean includesSauce, DeliveryDetails deliveryDetails) {
        super(pizzaName, unitPrice, quantity, diameterCm, includesSauce);
        this.deliveryDetails = Objects.requireNonNull(
                deliveryDetails, "Дані доставки не можуть бути null");
        System.out.println("Конструктор DeliveryOrder через super(...)");
    }

    public final String getDeliveryAddress() {
        return deliveryDetails.getAddress();
    }

    protected final DeliveryDetails getDeliveryDetails() {
        return deliveryDetails;
    }

    @Override
    public String getFulfillmentDescription() {
        return String.format("доставка на адресу «%s» (%.2f грн)",
                deliveryDetails.getAddress(), deliveryDetails.getDeliveryFee());
    }

    @Override
    public double getFulfillmentFee() {
        return deliveryDetails.getDeliveryFee();
    }

    @Override
    public String toString() {
        return super.toString() + String.format(", доставка на адресу «%s» (%.2f грн)",
                deliveryDetails.getAddress(), deliveryDetails.getDeliveryFee());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DeliveryOrder order)) {
            return false;
        }
        return super.equals(other) && Objects.equals(deliveryDetails, order.deliveryDetails);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), deliveryDetails);
    }
}
