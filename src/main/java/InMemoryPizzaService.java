package ua.edu.pizzeria;

import java.util.Comparator;
import java.util.List;

public class InMemoryPizzaService extends InMemoryCrudService<Pizza> implements PizzaService {
    @Override
    public List<Pizza> findAllSortedByNameAndByPrice() {
        return findAllSorted(Comparator.comparing(Pizza::getName)
                .thenComparingDouble(Pizza::getPrice));
    }
}
