public class DomainException extends Exception {
    private final String invalidValue;

    public DomainException(String message, String invalidValue) {
        super(message);
        this.invalidValue = invalidValue;
    }

    public String getInvalidValue() {
        return invalidValue;
    }
}