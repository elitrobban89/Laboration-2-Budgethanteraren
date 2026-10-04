import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate; //Behövs för filtrering på typ eller datum


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
     * Vi lagrar objekten i en ArrayList.
     */
    public List<T> findAll() {
        return new ArrayList<>(items);
    }

    /**
     * Filtrerar sparade objekt utifrån ett villkor.
     * @param villkor villkoret som objekten ska uppfylla
     * @return lista med objekt som uppfyller villkoret
     */
    public List<T> findWhere(Predicate<T> villkor) {
        return items.stream()
                .filter(villkor)
                .toList();
    }
}
