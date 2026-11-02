public class Order extends BaseEntity {
    private final int quantity;
    private final boolean extraSauce;

    public Order(long id, String name, int quantity, boolean extraSauce) {
        super(id, name);
        this.quantity = quantity;
        this.extraSauce = extraSauce;
    }

    public int getQuantity() { return quantity; }
    public boolean hasExtraSauce() { return extraSauce; }

    @Override
    public String toString() {
        return "Замовлення{id=" + getId() + ", назва='" + getName() + "', кількість="
                + quantity + ", додатковий соус=" + extraSauce + "}";
    }
}
