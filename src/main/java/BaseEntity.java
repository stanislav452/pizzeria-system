package ua.edu.pizzeria;

import java.util.Objects;

public abstract class BaseEntity {
    private final long id;
    private final String name;

    protected BaseEntity(long id, String name) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "name не може бути null");
    }

    public final long getId() { return id; }
    public final String getName() { return name; }

    @Override
    public final boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof BaseEntity)) return false;
        return id == ((BaseEntity) other).id;
    }

    @Override
    public final int hashCode() { return Long.hashCode(id); }
}
