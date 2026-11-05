package ua.edu.pizzeria;

import java.util.Comparator;
import java.util.List;

public class InMemoryPizzaOrderService extends InMemoryCrudService<PizzaOrder>
        implements PizzaOrderService {
    @Override
    public List<PizzaOrder> findAllSortedByNameAndByPrice() {
        return findAllSorted(Comparator.comparing(PizzaOrder::getName)
                .thenComparingDouble(PizzaOrder::getUnitPrice));
    }
}
