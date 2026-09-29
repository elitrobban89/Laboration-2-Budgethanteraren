import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RepositoryTest {
    @Test
    void testAdd_laggTillObjekt() {
        //Arrange (förbered)
        //Här använder vi ett Sträng objekt för att snabbt kunna testa
        Repository<String> repo = new Repository<>();
        String item = "Test-objekt";

        //Act(utför)
        repo.add(item);

        //Assert
        assertEquals(1, repo.findAll().size()); //Kollar om antalet ökar med 1
        assertTrue(repo.findAll().contains(item)); //Kollar att rätt innehåll finns
    }

    @Test
    void findAll() {
        //Arrange
        Repository<String> repo = new Repository<>(); //Tom Stränglista skapas

        //Act - det vi testar allItems innehåller tom lista
        var allItems = repo.findAll();

        //Assert Varför vi gör detta:
        assertTrue(allItems.isEmpty()); //Kollar att listan är tom. Om man får tillbaka en tom lista så ska inte programmet krascha.
    }

    @Test
    void findWhere() {
        //Arrange förberedelse
        Repository<String> repo = new Repository<>();
        repo.add("choklad");
        repo.add("Tårta");
        repo.add("avokado");
        repo.add("pendeltåg");

        //Act
        var borjarPaA = repo.findWhere(s -> s.startsWith("a"));
        var borjarPaStortT = repo.findWhere(s -> s.startsWith("T"));
        var borjarPaZ = repo.findWhere(s -> s.startsWith("z"));

        //Assert
        assertEquals(1, borjarPaA.size());
        assertTrue(borjarPaA.contains("avokado")); //Rätt objekt hittas

        assertEquals(1, borjarPaStortT.size());
        assertTrue(borjarPaStortT.contains("Tårta")); //Stor bokstav matchas

        assertTrue(borjarPaZ.isEmpty()); //Ingen träff ger tom lista, inte null
    }

    @Test
    void add_fleraObjekt_behallerOrdning() {
        //Arrange
        Repository<String> repo = new Repository<>();

        //Act
        repo.add("första");
        repo.add("andra");
        repo.add("tredje");

        //Assert - ArrayList ska behålla ordningen objekten lades till i
        assertEquals(List.of("första", "andra", "tredje"), repo.findAll());
    }

    @Test
    void add_dubbletter_tillats() {
        //Arrange
        Repository<String> repo = new Repository<>();

        //Act - samma objekt två gånger (t.ex. två likadana köp samma dag)
        repo.add("kaffe");
        repo.add("kaffe");

        //Assert
        assertEquals(2, repo.findAll().size());
    }

    @Test
    void findAll_returnerarKopia_repotPaverkasInte() {
        //Arrange
        Repository<String> repo = new Repository<>();
        repo.add("choklad");

        //Act - ändrar i listan vi fick tillbaka
        var lista = repo.findAll();
        lista.add("fusk");

        //Assert - repot ska inte gå att ändra utifrån
        assertEquals(1, repo.findAll().size());
        assertFalse(repo.findAll().contains("fusk"));
    }

    @Test
    void findWhere_tomtRepository_returnerarTomLista() {
        //Arrange - gränsfall: noll objekt
        Repository<String> repo = new Repository<>();

        //Act
        var resultat = repo.findWhere(s -> true);

        //Assert
        assertTrue(resultat.isEmpty()); //Ska inte krascha
    }

    @Test
    void findWhere_resultatetGarInteAttAndra() {
        //Arrange
        Repository<String> repo = new Repository<>();
        repo.add("choklad");

        //Act
        var resultat = repo.findWhere(s -> true);

        //Assert - toList() ger en oföränderlig lista
        assertThrows(UnsupportedOperationException.class, () -> resultat.add("fusk"));
    }

    @Test
    void findWhere_medHeltal_gransvarde() {
        //Arrange - visar att Repository<T> fungerar med andra typer (generics)
        Repository<Integer> repo = new Repository<>();
        repo.add(99);
        repo.add(100);
        repo.add(101);

        //Act - villkor "minst 100"
        var resultat = repo.findWhere(tal -> tal >= 100);

        //Assert - 100 ska räknas med, 99 ska inte det
        assertEquals(List.of(100, 101), resultat);
    }
}
