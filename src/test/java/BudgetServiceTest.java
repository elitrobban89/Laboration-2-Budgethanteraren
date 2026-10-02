import org.junit.jupiter.api.Test;

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
}
