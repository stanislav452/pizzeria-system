public class HappyHourPricingStrategy extends AbstractOrderPricingStrategy
        implements PromotionDescription {
    @Override
    public double calculateTotal(PizzaOrder order) {
        return applyDiscount(calculateSubtotal(order), 0.10);
    }

    @Override
    public String getName() {
        return "Година щасливих цін";
    }

    @Override
    public String getPromotionDescription() {
        return "Знижка 10% на все замовлення";
    }
}
