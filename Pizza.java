public class Pizza extends BaseEntity {
    private final double price;
    private final int diameterCm;

    public Pizza(long id, String name, double price, int diameterCm) {
        super(id, name);
        this.price = price;
        this.diameterCm = diameterCm;
    }

    public double getPrice() {
        return price;
    }

    public int getDiameterCm() {
        return diameterCm;
    }

    @Override
    public String toString() {
        return "Піца{id=" + getId() + ", назва='" + getName() + "', ціна=" + price
                + ", діаметр=" + diameterCm + " см}";
    }
}
