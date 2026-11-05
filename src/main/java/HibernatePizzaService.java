package ua.edu.pizzeria;

import java.util.Comparator;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

/**
 * Hibernate-реалізація {@link PizzaService} (третя реалізація поруч із in-memory і JDBC).
 * Клієнтський код не змінюється — змінюється лише обрана реалізація сервісу.
 */
public class HibernatePizzaService implements PizzaService {
    @Override
    public Pizza findById(long id) {
        return JpaUtil.readOnly(em -> {
            Pizza pizza = em.find(Pizza.class, id);
            if (pizza == null) {
                throw new EntityNotFoundException(id);
            }
            return pizza;
        });
    }

    @Override
    public List<Pizza> findAll() {
        return JpaUtil.readOnly(em -> em.createQuery(
                "SELECT p FROM Pizza p ORDER BY p.id", Pizza.class).getResultList());
    }

    @Override
    public Pizza save(Pizza pizza) {
        return JpaUtil.inTransaction(em -> {
            if (pizza.getId() == 0) {
                em.persist(pizza);
                return pizza;
            }
            Pizza managed = em.find(Pizza.class, pizza.getId());
            if (managed == null) {
                throw new EntityNotFoundException(pizza.getId());
            }
            return em.merge(pizza);
        });
    }

    @Override
    public void deleteById(long id) {
        JpaUtil.inTransaction(em -> {
            Pizza pizza = em.find(Pizza.class, id);
            if (pizza == null) {
                throw new EntityNotFoundException(id);
            }
            em.remove(pizza);
        });
    }

    @Override
    public List<Pizza> findAllSorted(Comparator<? super Pizza> comparator) {
        List<Pizza> result = findAll();
        result.sort(comparator);
        return result;
    }

    /** Сортування через JPQL ORDER BY (рівень 3). */
    @Override
    public List<Pizza> findAllSortedByNameAndByPrice() {
        return JpaUtil.readOnly(em -> em.createQuery(
                "SELECT p FROM Pizza p ORDER BY p.name, p.price", Pizza.class).getResultList());
    }

    /** Пошук за значенням поля іменованим параметром (рівень 3). */
    public List<Pizza> findByName(String name) {
        return JpaUtil.readOnly(em -> em.createQuery(
                "SELECT p FROM Pizza p WHERE p.name = :name", Pizza.class)
                .setParameter("name", name).getResultList());
    }

    /** Посторінкова вибірка (рівень 3). */
    public List<Pizza> findPage(int offset, int limit) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset має бути >= 0");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("limit має бути > 0");
        }
        return JpaUtil.readOnly(em -> em.createQuery(
                "SELECT p FROM Pizza p ORDER BY p.id", Pizza.class)
                .setFirstResult(offset).setMaxResults(limit).getResultList());
    }
}
