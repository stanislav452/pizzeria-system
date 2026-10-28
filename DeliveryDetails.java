import java.util.Objects;

public final class DeliveryDetails {
    private final String address;
    private final double deliveryFee;

    public DeliveryDetails(String address, double deliveryFee) {
        this.address = Objects.requireNonNull(address, "Адреса доставки не може бути null");
        if (address.isBlank()) {
            throw new IllegalArgumentException("Адреса доставки не може бути порожньою");
        }
        if (!Double.isFinite(deliveryFee) || deliveryFee < 0) {
            throw new IllegalArgumentException("Вартість доставки має бути скінченною та невід'ємною");
        }
        this.deliveryFee = deliveryFee;
    }

    public String getAddress() {
        return address;
    }

    public double getDeliveryFee() {
        return deliveryFee;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DeliveryDetails details)) {
            return false;
        }
        return Double.compare(deliveryFee, details.deliveryFee) == 0
                && Objects.equals(address, details.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, deliveryFee);
    }
}
