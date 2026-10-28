import java.util.Locale;
import java.util.Objects;

public final class OrderPricingStrategyFactory {
    public static final String STANDARD = "standard";
    public static final String HAPPY_HOUR = "happy-hour";

    private OrderPricingStrategyFactory() {
    }

    public static OrderPricingStrategy create(String type) {
        String normalizedType = Objects.requireNonNull(type, "Тип стратегії не може бути null")
                .trim()
                .toLowerCase(Locale.ROOT);
        switch (normalizedType) {
            case STANDARD:
                return new StandardOrderPricingStrategy();
            case HAPPY_HOUR:
                return new HappyHourPricingStrategy();
            default:
                throw new IllegalArgumentException("Невідомий тип стратегії ціноутворення: " + type);
        }
    }
}
