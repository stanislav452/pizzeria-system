import java.util.Objects;

public abstract class AbstractOrderPricingStrategy implements OrderPricingStrategy {
    protected final double calculateSubtotal(PizzaOrder order) {
        Objects.requireNonNull(order, "Замовлення не може бути null");
        return (order.getUnitPrice() + (order.includesSauce() ? 25.0 : 0.0))
                * order.getQuantity() + order.getFulfillmentFee();
    }

    protected final double applyDiscount(double subtotal, double discountRate) {
        return subtotal * (1.0 - discountRate);
    }
}
