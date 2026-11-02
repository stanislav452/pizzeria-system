import java.util.List;

public interface OrderService extends CRUDService<Order> {
    List<Order> findAllSortedByNameAndByQuantity();
}
