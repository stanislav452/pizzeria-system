package ua.edu.pizzeria;

import java.util.Comparator;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

/**
 * Hibernate-реалізація {@link OrderService} (третя реалізація).
 * Друга сутність із зв'язком @ManyToOne до Pizza.
 */
public class HibernateOrderService implements OrderService {
    @Override
    public Order findById(long id) {
        return JpaUtil.readOnly(em -> {
            Order order = em.find(Order.class, id);
            if (order == null) {
                throw new EntityNotFoundException(id);
            }
            return order;
        });
    }

    @Override
    public List<Order> findAll() {
        return JpaUtil.readOnly(em -> em.createQuery(
                "SELECT o FROM Order o ORDER BY o.id", Order.class).getResultList());
    }

    @Override
    public Order save(Order order) {
        return JpaUtil.inTransaction(em -> {
            if (order.getId() == 0) {
                em.persist(order);
                return order;
            }
            Order managed = em.find(Order.class, order.getId());
            if (managed == null) {
                throw new EntityNotFoundException(order.getId());
            }
            return em.merge(order);
        });
    }

    @Override
    public void deleteById(long id) {
        JpaUtil.inTransaction(em -> {
            Order order = em.find(Order.class, id);
            if (order == null) {
                throw new EntityNotFoundException(id);
            }
            em.remove(order);
        });
    }

    @Override
    public List<Order> findAllSorted(Comparator<? super Order> comparator) {
        List<Order> result = findAll();
        result.sort(comparator);
        return result;
    }

    /** Сортування через JPQL ORDER BY (рівень 3). */
    @Override
    public List<Order> findAllSortedByNameAndByQuantity() {
        return JpaUtil.readOnly(em -> em.createQuery(
                "SELECT o FROM Order o ORDER BY o.name, o.quantity", Order.class).getResultList());
    }

    /** Пошук за назвою піци іменованим параметром (рівень 3). */
    public List<Order> findByPizzaName(String pizzaName) {
        return JpaUtil.readOnly(em -> em.createQuery(
                "SELECT o FROM Order o WHERE o.pizza.name = :pizzaName", Order.class)
                .setParameter("pizzaName", pizzaName).getResultList());
    }

    /** Посторінкова вибірка (рівень 3). */
    public List<Order> findPage(int offset, int limit) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset має бути >= 0");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("limit має бути > 0");
        }
        return JpaUtil.readOnly(em -> em.createQuery(
                "SELECT o FROM Order o ORDER BY o.id", Order.class)
                .setFirstResult(offset).setMaxResults(limit).getResultList());
    }
}
