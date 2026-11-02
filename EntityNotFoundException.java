public class EntityNotFoundException extends RuntimeException {
    private final long id;

    public EntityNotFoundException(long id) {
        super("Сутність з id=" + id + " не знайдена");
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
