package ua.edu.pizzeria;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Замовлення піци. Відповідає таблиці {@code orders} із init.sql ЛР №10.
 * Зв'язок "багато замовлень до однієї піци" — {@link ManyToOne} на {@link Pizza}.
 * Власником зв'язку є бік Order: стовпець {@code orders.name} (зовнішній ключ)
 * належить таблиці orders.
 */
@Entity
@Table(name = "orders")
@AttributeOverride(
        name = "name",
        column = @Column(name = "name", nullable = false, length = 120, insertable = false, updatable = false)
)
public class Order extends BaseEntity {
    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "extra_sauce", nullable = false)
    private boolean extraSauce;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "name", referencedColumnName = "name", nullable = false)
    private Pizza pizza;

    protected Order() {
    }

    public Order(long id, String name, int quantity, boolean extraSauce) {
        super(name);
        setId(id);
        setQuantity(quantity);
        setExtraSauce(extraSauce);
    }

    public Order(Pizza pizza, int quantity, boolean extraSauce) {
        super(pizza.getName());
        setPizza(pizza);
        setQuantity(quantity);
        setExtraSauce(extraSauce);
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Кількість має бути більше нуля");
        }
        this.quantity = quantity;
    }

    public boolean hasExtraSauce() {
        return extraSauce;
    }

    public void setExtraSauce(boolean extraSauce) {
        this.extraSauce = extraSauce;
    }

    public Pizza getPizza() {
        return pizza;
    }

    public void setPizza(Pizza pizza) {
        this.pizza = Objects.requireNonNull(pizza, "Піца не може бути null");
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Order that)) {
            return false;
        }
        return quantity == that.quantity
                && extraSauce == that.extraSauce
                && Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), quantity, extraSauce);
    }

    @Override
    public String toString() {
        return "Замовлення{id=" + getId() + ", назва='" + getName() + "', кількість=" + quantity
                + ", соус=" + extraSauce + ", піца=" + (pizza != null ? pizza.getName() : "null") + "}";
    }
}
