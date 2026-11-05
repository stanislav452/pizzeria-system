package ua.edu.pizzeria;

public interface OrderStatusListener {
    void onStatusChanged(PizzaOrder order, OrderStatus previousStatus, OrderStatus newStatus);
}
