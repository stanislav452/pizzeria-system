import java.util.Objects;

public class OrderCheckout {
    private OrderPricingStrategy pricingStrategy;

    public OrderCheckout(OrderPricingStrategy pricingStrategy) {
        setPricingStrategy(pricingStrategy);
    }

    public void setPricingStrategy(OrderPricingStrategy pricingStrategy) {
        this.pricingStrategy = Objects.requireNonNull(pricingStrategy, "Стратегія розрахунку не може бути null");
    }

    public double calculateTotal(PizzaOrder order) {
        return pricingStrategy.calculateTotal(order);
    }

    public String getStrategyName() {
        return pricingStrategy.getName();
    }
}
