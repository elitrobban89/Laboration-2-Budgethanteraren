import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TransaktionValidatorTest {
    /**
     * Vid direkt anrop till parseBelopp eller validate måste
     * testet deklarera InvalidTransactionException med throws.
     *
     * @throws InvalidTransactionException
     */
    @Test
    void testParseBelopp_trim_siffror() throws InvalidTransactionException {
        //Arrange
        String beloppStr = " 100 "; //testar även trim och sträng parsning till belopp

        //Act
        double belopp = TransaktionValidator.parseBelopp(beloppStr);

        //Assert
        assertEquals(100, belopp); //Ska bli rätt tal
    }

    @Test
    void testParseBelopp_null() { //Kräver inte throws då lambda används
        //Act+assert i detta fallet
        assertThrows(InvalidTransactionException.class, () -> TransaktionValidator.parseBelopp(null));
    }

    @Test
    void testParseBelopp_bokstaver() { //Kräver inte throws då lambda används
        //Arrange
        String beloppStr = " abc "; //Bokstäver kan aldrig bli tal. Testar även trim.

        //Act+Assert
        InvalidTransactionException e = assertThrows(InvalidTransactionException.class,
                () -> TransaktionValidator.parseBelopp(beloppStr));
        assertEquals("Felaktigt format på belopp!", e.getMessage());
    }

    @Test
    void testValidate_beloppNoll() {
        //Act + Assert är 0 på gränsen
        InvalidTransactionException e = assertThrows(InvalidTransactionException.class,
                () -> TransaktionValidator.validate(0, "Mat"));
        assertEquals("Beloppet måste vara större än 0", e.getMessage());
    }

    @Test
    void testValidate_beloppNegativt() {
        //Act + Assert Negativt belopp ska inte godkännas
        assertThrows(InvalidTransactionException.class,
                () -> TransaktionValidator.validate(-5, "Mat"));
    }

    @Test
    void testValidate_minstaGiltigaBelopp() { //Motsatsen till assertThrows. Testet går igenom om inget undantag kastas.
        //Act + Assert är 0.01 över gränsen
        assertDoesNotThrow(() -> TransaktionValidator.validate(0.01, "Mat"));
    }
}