public class StandardOrderPricingStrategy extends AbstractOrderPricingStrategy {
    @Override
    public double calculateTotal(PizzaOrder order) {
        double subtotal = calculateSubtotal(order);
        double discountRate = 0.0;
        if (order.getQuantity() >= 3 || subtotal >= 600.0) {
            discountRate = 0.15;
        } else if (order.getQuantity() == 2) {
            discountRate = 0.05;
        }
        return applyDiscount(subtotal, discountRate);
    }
}
