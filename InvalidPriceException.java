public class InvalidPriceException extends DomainException {
    public InvalidPriceException(double price) {
        super("ціна піци має бути додатною та скінченною", String.valueOf(price));
    }
}