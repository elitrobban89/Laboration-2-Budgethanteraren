import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate; //Behövs för filtrering på typ eller datum


/**
 * Repository<T> är generisk, så den kan lagra vilken typ som helst. Det visar jag i testet med Integer.
 * Inuti använder den en ArrayList som collection.
 * BudgetService använder en Map (HashMap) för att summera per kategori, och List för filtrering och sortering."
 *
 * @param <T> typparameter (Generics).
 * Fungerar som en platshållare för vilken datatyp som helst.
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
     * Returnerar en kopia av listan med alla sparade objekt. Så att ingen utanför klassen kan ändra på listan.
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
