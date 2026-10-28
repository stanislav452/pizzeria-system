import java.util.Objects;

public class KitchenDisplayAdapter implements OrderStatusListener {
    private final LegacyKitchenDisplay legacyDisplay;

    public KitchenDisplayAdapter(LegacyKitchenDisplay legacyDisplay) {
        this.legacyDisplay = Objects.requireNonNull(legacyDisplay, "Кухонний дисплей не може бути null");
    }

    @Override
    public void onStatusChanged(PizzaOrder order, OrderStatus previousStatus, OrderStatus newStatus) {
        legacyDisplay.displayOrder(order.getPizzaName(), newStatus.getDisplayName());
    }
}
