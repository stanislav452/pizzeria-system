package ua.edu.pizzeria;

import java.math.BigDecimal;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Спігурована сутність з розбіжністю від реальної таблиці pizzas:
 * сутність має поле {@code phone}, якого немає у таблиці {@code pizzas}
 * (також price має scale = 3 замість scale = 2).
 * Використовується лише у HibernateValidateDemo для demonstracії помилки hibernate.hbm2ddl.auto=validate.
 */
@Entity
@Table(name = "pizzas")
public class PizzaMismatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "price", nullable = false, precision = 10, scale = 3)
    private BigDecimal price;

    @Column(name = "phone", length = 20)
    private String phone;

    protected PizzaMismatch() {
    }

    public PizzaMismatch(String name, double price) {
        this.name = name;
        this.price = BigDecimal.valueOf(price);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof PizzaMismatch that)) return false;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
