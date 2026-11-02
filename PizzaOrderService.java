import java.util.List;

public interface PizzaOrderService extends CRUDService<PizzaOrder> {
    List<PizzaOrder> findAllSortedByNameAndByPrice();
}
