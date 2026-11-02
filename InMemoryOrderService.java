import java.util.Comparator;
import java.util.List;

public class InMemoryOrderService extends InMemoryCrudService<Order> implements OrderService {
    @Override
    public List<Order> findAllSortedByNameAndByQuantity() {
        return findAllSorted(Comparator.comparing(Order::getName)
                .thenComparingInt(Order::getQuantity));
    }
}
