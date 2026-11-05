package ua.edu.pizzeria;

public interface OrderPricingStrategy {
    double calculateTotal(PizzaOrder order);

    default String getName() {
        return "Стандартний розрахунок";
    }
}
