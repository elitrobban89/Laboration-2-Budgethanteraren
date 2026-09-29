import java.util.ArrayList;
import java.util.List;

/**
 * @param <T> typparameter (Generics).
 *            Den fungerar som en platshållare för vilken datatyp som helst.
 */
public class Repository<T> {
    private final List<T> items = new ArrayList<>();

    /**
     * Man ska kunna lägga till objekt i arraylisten.
     *
     * @param item objektet som ska läggas till
     */

    public void add(T item) {
        items.add(item);
    }

    /**
     * Man ska kunna hitta alla sparade objekt i samlingen.
     *
     */
    public List<T> findAll() {
        return new ArrayList<>(items);
    }

}
