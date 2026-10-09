import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TransaktionFilHanterareTest {
    @TempDir //Temporär mapp ställe där vi lagrar filer
    Path tempDir;

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
     *
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

    @Test
    public void testSparaOchLas_sammaTransaktionerTillbaka() throws IOException {

        //Arrange:
        TransaktionFilHanterare filHanterare = new TransaktionFilHanterare();
        Path fil = tempDir.resolve("transaktioner.csv");
        List<Transaktion> transaktioner = List.of(
                new Transaktion(LocalDate.of(2026, 10, 1), "Lön", 25000.0, TransaktionTyp.INKOMST),
                new Transaktion(LocalDate.of(2026, 10, 3), "Mat", 842.5, TransaktionTyp.UTGIFT));

        //Act:
        filHanterare.spara(transaktioner,fil);
        List<Transaktion> inlasta = filHanterare.las(fil);

        //Assert:
        assertEquals(transaktioner,inlasta);
    }
    @Test
    public void testLas_filSaknas_skaparTomFil() throws IOException {
        //Arrange: en sökväg till en fil som inte finns än
        TransaktionFilHanterare filHanterare = new TransaktionFilHanterare();
        Path fil = tempDir.resolve("finnsInte.csv");

        //Act:
        List<Transaktion> inlasta = filHanterare.las(fil);

        //Assert: tom lista, och filen har skapats
        assertTrue(inlasta.isEmpty());
        assertTrue(Files.exists(fil));
    }

    @Test
    public void testLas_trasigRad_hoppasOver() throws IOException {
        //Arrange: en fil med två hela rader och en trasig rad emellan
        TransaktionFilHanterare filHanterare = new TransaktionFilHanterare();
        Path fil = tempDir.resolve("trasig.csv");
        Files.writeString(fil, """
                2026-10-01;Lön;25000.0;INKOMST
                det här är ingen transaktion
                2026-10-03;Mat;842.5;UTGIFT
                """);

        //Act:
        List<Transaktion> inlasta = filHanterare.las(fil);

        //Assert: de två hela raderna kom med, den trasiga hoppades över
        assertEquals(2, inlasta.size());
        assertEquals("Lön", inlasta.get(0).kategori());
        assertEquals("Mat", inlasta.get(1).kategori());
    }
}