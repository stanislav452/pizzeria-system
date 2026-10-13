public class OrderStatusLogger implements OrderStatusListener {
    @Override
    public void onStatusChanged(PizzaOrder order, OrderStatus previousStatus, OrderStatus newStatus) {
        System.out.printf("Журнал: замовлення «%s» змінило статус з «%s» на «%s».%n",
                order.getPizzaName(), previousStatus.getDisplayName(), newStatus.getDisplayName());
    }
}
