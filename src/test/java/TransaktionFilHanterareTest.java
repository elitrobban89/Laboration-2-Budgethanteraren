import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TransaktionFilHanterareTest {

    /**
     * Testar att en transaktion kan omvandlas till en rad i CSV format.
     */
    @Test
    public void testTillCsvRad_ger_ratt_format() {
        //Arrange:
        TransaktionFilHanterare filHanterare = new TransaktionFilHanterare();
        Transaktion transaktion = new Transaktion(LocalDate.of(2022, 1, 1), "Kategori", 100.0, TransaktionTyp.UTGIFT);
        String expected = "2022-01-01;Kategori;100.0;UTGIFT";

        //Act:
        String actual = filHanterare.tillCsvRad(transaktion);

        //Assert:
        assertEquals(expected, actual);
    }

    /**
     * Testar att en rad från CSV format kan omvandlas till en transaktion. (åt andra hållet än testet ovan)
     * @throws FileFormatException om raden inte har rätt format
     */
    @Test
    public void testFranCsvRad_helRad_gerTransaktion() throws FileFormatException {
        //Arrange:
        TransaktionFilHanterare filHanterare = new TransaktionFilHanterare();
        String rad = "2022-01-01;Kategori;100.0;UTGIFT";
        Transaktion expected = new Transaktion(LocalDate.of(2022, 1, 1), "Kategori", 100.0, TransaktionTyp.UTGIFT);

        //Act:
        Transaktion actual = filHanterare.franCsvRad(rad);

        //Assert:
        assertEquals(expected, actual);
    }

    @Test
    public void testFranCsvRad_felAntalFalt_kastarFileFormatException() {
        //Arrange: om raden saknar typ. Alltså den har bara tre fält istället för fyra. CSV-raden är alltså trasig.
        TransaktionFilHanterare filHanterare = new TransaktionFilHanterare();
        String rad = "2022-01-01;Kategori;100.0";

        //Act och Assert:
        assertThrows(FileFormatException.class, () -> filHanterare.franCsvRad(rad));
    }

    /**
     * Double.parseDouble kan inte göra om abc (en text) till ett tal.
     * Java kastar NumberFormatException. Alltså hade programmet kraschat istället för kastat FileFormatException som vi förväntade oss.
     */
    @Test
    public void testFranCsvRad_beloppInteTal_kastarFileFormatException() {
        //Arrange: rätt antal fält, men beloppet är text i stället för ett tal
        TransaktionFilHanterare filHanterare = new TransaktionFilHanterare();
        String rad = "2022-01-01;Kategori;abc;UTGIFT";

        //Act och Assert:
        assertThrows(FileFormatException.class, () -> filHanterare.franCsvRad(rad));
    }
}