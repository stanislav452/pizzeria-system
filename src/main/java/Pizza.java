package ua.edu.pizzeria;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.hibernate.Hibernate;

/**
 * Піца з меню. Відповідає таблиці {@code pizzas} із init.sql ЛР №10.
 * Клас не final, має конструктор без параметрів (потрібен Hibernate).
 *
 * <p>Зв'язок "одна піця — багато замовлень" (інверсний бік):
 * {@code orders} має владельцем бік Order.pizza ({@code mappedBy = "pizza"}).
 *
 * <p>Ціна зберігається як {@link BigDecimal}, щоб відповідати {@code NUMERIC(10, 2)}
 * у БД (гроші ніколи не {@code FLOAT}/{@code double} на стороні сховища).
 */
@Entity
@Table(name = "pizzas")
public class Pizza extends BaseEntity {
    @Column(name = "price", nullable = false, precision = 10)
    private BigDecimal price;

    @Column(name = "diameter_cm", nullable = false)
    private int diameterCm;

    @OneToMany(mappedBy = "pizza", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    protected Pizza() {
    }

    public Pizza(long id, String name, double price, int diameterCm) {
        this(name, price, diameterCm);
        setId(id);
    }

    public Pizza(String name, double price, int diameterCm) {
        super(name);
        setPrice(price);
        setDiameterCm(diameterCm);
    }

    public double getPrice() {
        return price.doubleValue();
    }

    public void setPrice(double price) {
        if (!Double.isFinite(price) || price <= 0) {
            throw new IllegalArgumentException("Ціна має бути додатньою та скінченною");
        }
        this.price = BigDecimal.valueOf(price);
    }

    public BigDecimal getPriceBigDecimal() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = Objects.requireNonNull(price, "Ціна не може бути null");
    }

    public int getDiameterCm() {
        return diameterCm;
    }

    public void setDiameterCm(int diameterCm) {
        if (diameterCm <= 0) {
            throw new IllegalArgumentException("Діаметр має бути додатнім");
        }
        this.diameterCm = diameterCm;
    }

    public List<Order> getOrders() {
        return orders;
    }

    /**
     * Допоміжний метод: узгоджує обидві сторони зв'язку.
     */
    public void addOrder(Order order) {
        Objects.requireNonNull(order, "Замовлення не може бути null");
        if (Hibernate.isInitialized(orders)) {
            if (!orders.contains(order)) {
                orders.add(order);
            }
        } else {
            // Сутність від'єднана і колекція не завантажена: міркуємо, що замовлення ще не додане.
            List<Order> materialized = new ArrayList<>();
            materialized.add(order);
            this.orders = materialized;
        }
        order.setPizza(this);
    }

    /**
     * Допоміжний метод: узгоджує обидві сторони зв'язку.
     */
    public void removeOrder(Order order) {
        if (Hibernate.isInitialized(orders) && orders.remove(order)) {
            order.setPizza(null);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Pizza that)) {
            return false;
        }
        return Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }

    @Override
    public String toString() {
        return "Піца{id=" + getId() + ", назва='" + getName() + "', ціна=" + price
                + ", діаметр=" + diameterCm + " см}";
    }
}
