import java.util.Objects;

public abstract class BaseEntity {
    private final long id;
    private final String name;

    protected BaseEntity(long id, String name) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "name не може бути null");
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof BaseEntity)) {
            return false;
        }
        BaseEntity that = (BaseEntity) object;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
