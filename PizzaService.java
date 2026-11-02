import java.util.Comparator;
import java.util.List;

public interface PizzaService extends CRUDService<Pizza> {
    List<Pizza> findAllSortedByNameAndByPrice();
}
