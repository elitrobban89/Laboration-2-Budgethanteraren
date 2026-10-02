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
}
