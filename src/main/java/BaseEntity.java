package ua.edu.pizzeria;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.util.Objects;

/**
 * Спільна основа сутностей (id та назва), як у ЛР №7.
 * Відображується як {@link MappedSuperclass}: власних таблиць не створює,
 * поля успадковуються Pizza та Order.
 */
@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    protected BaseEntity() {
    }

    protected BaseEntity(long id, String name) {
        this.id = id;
        setName(name);
    }

    protected BaseEntity(String name) {
        this(0, name);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name не може бути null");
    }

    protected void setId(long id) {
        this.id = id;
    }

    /**
     * Рівність за бізнес-ключем (назвою), а не за id:
     * id сутності призначається БД лише після першого flush, тому до цього
     * моменту порівняння за id блохане. Назва піци унікальна (UNIQUE в БД),
     * тому вона підходить як бізнес-ключ для Pizza.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof BaseEntity that)) {
            return false;
        }
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + id + ", name='" + name + "'}";
    }
}
