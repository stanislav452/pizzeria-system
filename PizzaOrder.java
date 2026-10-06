import java.util.Objects;

public class PizzaOrder {
    private static final OrderPricingStrategy DEFAULT_PRICING_STRATEGY = new StandardOrderPricingStrategy();

    private final String pizzaName;
    private final double unitPrice;
    private final int quantity;
    private final int diameterCm;
    private final boolean includesSauce;

    public PizzaOrder(String pizzaName, double unitPrice, int quantity, int diameterCm, boolean includesSauce)
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

    public double getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public boolean includesSauce() {
        return includesSauce;
    }

    @Override
    public String toString() {
        double total = DEFAULT_PRICING_STRATEGY.calculateTotal(this);
        return String.format(
                "%s (%d см), ціна: %.2f грн/шт, кількість: %d, соус: %s, сума: %.2f грн",
                pizzaName, diameterCm, unitPrice, quantity, includesSauce ? "так" : "ні",
                total);
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