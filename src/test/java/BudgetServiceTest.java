import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

public class BudgetServiceTest {
    @Test
    void testSaldo_ingaTransaktioner() {
//Arrange - Ifall man skickar in ett tomt repo och BudgetService räknar på det
        Repository<Transaktion> repo = new Repository<>();
        BudgetService budgetService = new BudgetService(repo);

        //Act
        double saldo = budgetService.saldo();

        //Assert. Inga transaktioner ska ge saldo 0, inte krascha programmet.
        assertEquals(0.0, saldo);
    }

    @Test
    void testSaldo_inkomsterMinusUtgifter() {
        //Arrange en inkomst två utgifter
        Repository<Transaktion> repo = new Repository<>();
        repo.add(new Transaktion(LocalDate.now(), "Lön", 25000, TransaktionTyp.INKOMST));
        repo.add(new Transaktion(LocalDate.now(), "Mat", 842.50, TransaktionTyp.UTGIFT));
        repo.add(new Transaktion(LocalDate.now(), "Hyra", 7200, TransaktionTyp.UTGIFT));
        BudgetService budgetService = new BudgetService(repo);

        //Act
        double saldo = budgetService.saldo();

        //Assert - 25000 - 842.50 - 7200 = 16957.50
        assertEquals(16957.50, saldo);
    }

    @Test
    void testSummaPerKategori_sammaKategoriSlasIhop() {
        //Arrange - Tre utgifter: två transaktioner i Mat och en i Hyra
        Repository<Transaktion> repo = new Repository<>();
        repo.add(new Transaktion(LocalDate.now(), "Mat", 842.50, TransaktionTyp.UTGIFT));
        repo.add(new Transaktion(LocalDate.now(), "Mat", 250, TransaktionTyp.UTGIFT));
        repo.add(new Transaktion(LocalDate.now(), "Hyra", 7200, TransaktionTyp.UTGIFT));
        BudgetService budgetService = new BudgetService(repo);

        //Act
        Map<String, Double> perKategori = budgetService.summaPerKategori();

        //Assert
        assertEquals(2, perKategori.size());            //Två kategorier, inte tre transaktioner
        assertEquals(1092.50, perKategori.get("Mat"));  //842.50 + 250 slås ihop
        assertEquals(7200.0, perKategori.get("Hyra"));
    }

    @Test
    void testSummaPerKategori_ingaTransaktioner() {
        //Arrange - tomt repo
        Repository<Transaktion> repo = new Repository<>();
        BudgetService budgetService = new BudgetService(repo);

        //Act
        Map<String, Double> perKategori = budgetService.summaPerKategori();

        //Assert - tom Map, inte null
        assertNotNull(perKategori);
        assertTrue(perKategori.isEmpty());
    }

    @Test
    void testFiltreraTyp_baraUtgifter() {
        //Arrange - en inkomst och två utgifter
        Repository<Transaktion> repo = new Repository<>();
        repo.add(new Transaktion(LocalDate.now(), "Lön", 25000, TransaktionTyp.INKOMST));
        repo.add(new Transaktion(LocalDate.now(), "Mat", 842.50, TransaktionTyp.UTGIFT));
        repo.add(new Transaktion(LocalDate.now(), "Hyra", 7200, TransaktionTyp.UTGIFT));
        BudgetService budgetService = new BudgetService(repo);

        //Act
        List<Transaktion> utgifter = budgetService.filtreraTyp(TransaktionTyp.UTGIFT);

        //Assert
        assertEquals(2, utgifter.size()); //Bara de två utgifterna, inte lönen
        assertTrue(utgifter.stream().allMatch(t -> t.typ() == TransaktionTyp.UTGIFT)); //Alla är utgifter
    }

    @Test
    void testFiltreraDatum_gransdagarRaknasMed() {
        //Arrange - en transaktion på varje gräns, en i mitten och två utanför
        Repository<Transaktion> repo = new Repository<>();
        repo.add(new Transaktion(LocalDate.of(2026, 9, 30), "Före", 100, TransaktionTyp.UTGIFT));  //dagen före intervallet
        repo.add(new Transaktion(LocalDate.of(2026, 10, 1), "Start", 100, TransaktionTyp.UTGIFT)); //första dagen
        repo.add(new Transaktion(LocalDate.of(2026, 10, 15), "Mitt", 100, TransaktionTyp.UTGIFT)); //mitt i
        repo.add(new Transaktion(LocalDate.of(2026, 10, 31), "Slut", 100, TransaktionTyp.UTGIFT)); //sista dagen
        repo.add(new Transaktion(LocalDate.of(2026, 11, 1), "Efter", 100, TransaktionTyp.UTGIFT)); //dagen efter intervallet
        BudgetService budgetService = new BudgetService(repo);

        //Act - hela oktober
        List<Transaktion> oktober = budgetService.filtreraDatum(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));

        //Assert - Start, Mitt och Slut ska med. Före och Efter ska inte med.
        assertEquals(3, oktober.size());
    }
}
