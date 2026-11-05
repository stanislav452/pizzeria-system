package ua.edu.pizzeria;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class InMemoryCrudService<T extends BaseEntity> implements CRUDService<T> {
    private final Map<Long, T> storage = new LinkedHashMap<>();

    @Override
    public T findById(long id) {
        T entity = storage.get(id);
        if (entity == null) throw new EntityNotFoundException(id);
        return entity;
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public T save(T entity) {
        Objects.requireNonNull(entity, "entity не може бути null");
        storage.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public void deleteById(long id) {
        if (storage.remove(id) == null) throw new EntityNotFoundException(id);
    }

    @Override
    public List<T> findAllSorted(Comparator<? super T> comparator) {
        List<T> result = findAll();
        result.sort(Objects.requireNonNull(comparator, "comparator не може бути null"));
        return result;
    }
}
