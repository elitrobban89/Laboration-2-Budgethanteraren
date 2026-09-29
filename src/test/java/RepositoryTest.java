import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositoryTest {
    @Test
    void testAdd() {
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
}