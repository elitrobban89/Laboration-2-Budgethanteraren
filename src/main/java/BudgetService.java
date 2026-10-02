import java.util.Map;
import java.util.stream.Collectors;

/**
 * Räknar på transaktionerna i ett Repository: saldo och summa per kategori m.m.
 * Kan testas med Junit då den inte läser från tagentbordet.
 */
public class BudgetService {
    private final Repository<Transaktion> repository;

    //Repositoryt som används för att hämta transaktioner.
    public BudgetService(Repository<Transaktion> repository) {
        this.repository = repository;
    }

    /**
     * Räknar ut saldot. Alla inkomster minus alla utgifter.
     * Returnera 0 om det inte finns några transaktioner.
     * Belopp sparas som rätt typ utan minustecknet om det ex är en utgift 842.5
     */
    public double saldo() {
        return repository.findAll().stream().mapToDouble(t->t.typ() ==
                TransaktionTyp.INKOMST ? t.belopp() : -t.belopp()).sum();
    }

    /**
     * Summerar transaktionerna per kategori.
     * @return är en Map med kategori och summan av transaktionerna. Tom Map om det inte finns några transaktioner.
     */
    public Map<String, Double> summaPerKategori() {
        return repository.findAll().stream()
                .collect(Collectors.groupingBy(t -> t.kategori(), //Gruppera efter kategori. Metoden läser fältet.
                        Collectors.summingDouble(t->t.belopp()))); //Summera beloppen för varje kategori
    }
}
