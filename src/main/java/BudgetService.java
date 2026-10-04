import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Räknar på transaktionerna i ett Repository: saldo och summa per kategori m.m.
 * Kan testas med JUnit.
 */
public class BudgetService {
    private final Repository<Transaktion> repository;

    //Repositoryt som används för att hämta transaktioner.
    public BudgetService(Repository<Transaktion> repository) {
        this.repository = repository;
    }

    /**
     * Menyval 3
     * Räknar ut saldot. Alla inkomster minus alla utgifter.
     * Returnera 0 om det inte finns några transaktioner.
     * Belopp sparas alltid positivt t ex 842.5 och inte -842.5 för en utgift. Det är typen INKOMST/UTGIFT som avgör om beloppet ska plussas eller minusas.
     */
    public double saldo() {
        return repository.findAll().stream().mapToDouble(t -> t.typ() ==
                TransaktionTyp.INKOMST ? t.belopp() : -t.belopp()).sum();
    }

    /**
     * Menyval 3
     * Summerar transaktionerna per kategori.
     * Här använder vi groupingBy som skapar en HashMap åt oss. Nyckeln är kategori String värdet är summan double.
     * Eftersom att det är en hashMap är ordningen som kategorierna skrivs ut i inte garanterad.
     * @return är en Map med kategori och summan av transaktionerna. Tom Map om det inte finns några transaktioner.
     */
    public Map<String, Double> summaPerKategori() {
        return repository.findAll().stream()
                .collect(Collectors.groupingBy(t -> t.kategori(), // Nyckeln är kategori (String)
                        Collectors.summingDouble(t -> t.belopp()))); //Summera beloppen för varje kategori
    }

    /**
     * Menyval 4 Filtrerar transaktionerna efter typ: INKOMST eller UTGIFT.
     *
     * @param typ INKOMST eller UTGIFT
     * @return lista med transaktioner av den typen, tom lista om det inte finns några
     */
    public List<Transaktion> filtreraTyp(TransaktionTyp typ) {
        return repository.findWhere(t -> t.typ() == typ); //Använder findWhere-metoden i Repository
    }

    /**
     * Menyval 4 Filtrerar transaktionerna på ett datumintervall.
     *
     * @param start datum för start av intervallet
     * @param slut  datum för slutet av intervallet
     * @return lista med transaktioner inom intervallet, tom lista om det inte finns några
     */
    public List<Transaktion> filtreraDatum(LocalDate start, LocalDate slut) { //Vilka transaktioner som ska visas
        return repository.findWhere(t -> !t.datum().isBefore(start) && !t.datum().isAfter(slut)); //Åtgärd av bugg om datumet är gränsdagen ska vi få med transaktionen i filtret
    }

    /**
     * Hämtar alla transaktioner sorterade på datum äldst först.
     * Påverkar inte ordningen på transaktionerna i repositoryt. Sorterar en kopia.
     *
     * @return ny lista med transaktioner sorterade på datum
     */
    public List<Transaktion> sorteraPaDatum() {
        return repository.findAll().stream().sorted(Comparator.comparing(t -> t.datum())).toList();
    }
}
