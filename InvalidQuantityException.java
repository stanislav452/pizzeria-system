public class InvalidQuantityException extends DomainException {
    public InvalidQuantityException(int quantity) {
        super("кількість має бути більшою за нуль", String.valueOf(quantity));
    }
}