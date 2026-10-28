public final class ExpressDeliveryOrder extends DeliveryOrder {
    private final double expressFee;

    public ExpressDeliveryOrder(String pizzaName, double unitPrice, int quantity, int diameterCm,
                                boolean includesSauce, String address, double deliveryFee,
                                double expressFee) {
        super(pizzaName, unitPrice, quantity, diameterCm, includesSauce, address, deliveryFee);
        if (!Double.isFinite(expressFee) || expressFee < 0) {
            throw new IllegalArgumentException("Доплата за експрес-доставку має бути скінченною та невід'ємною");
        }
        this.expressFee = expressFee;
        System.out.println("Конструктор ExpressDeliveryOrder через super(...)");
    }

    public double getExpressFee() {
        return expressFee;
    }

    @Override
    public String getFulfillmentDescription() {
        return super.getFulfillmentDescription()
                + String.format(", експрес-доплата: %.2f грн", expressFee);
    }

    @Override
    public double getFulfillmentFee() {
        return super.getFulfillmentFee() + expressFee;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(", експрес-доплата: %.2f грн", expressFee);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ExpressDeliveryOrder order)) {
            return false;
        }
        return super.equals(other) && Double.compare(expressFee, order.expressFee) == 0;
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Double.hashCode(expressFee);
    }
}
