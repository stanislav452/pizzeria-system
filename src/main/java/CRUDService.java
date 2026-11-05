package ua.edu.pizzeria;

import java.util.Comparator;
import java.util.List;

public interface CRUDService<T extends BaseEntity> {
    T findById(long id);
    List<T> findAll();
    T save(T entity);
    void deleteById(long id);
    List<T> findAllSorted(Comparator<? super T> comparator);

    default List<T> findAllSortedByName() {
        return findAllSorted(Comparator.comparing(BaseEntity::getName));
    }
}
