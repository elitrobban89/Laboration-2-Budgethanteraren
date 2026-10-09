import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class TransaktionFilHanterareTest {

    @Test
    public void testTillCsvRad_ger_ratt_format() {
        //Arrange:
        TransaktionFilHanterare filHanterare = new TransaktionFilHanterare();
        Transaktion transaktion = new Transaktion(LocalDate.of(2022,1,1), "Kategori", 100.0, TransaktionTyp.UTGIFT);
        String expected = "2022-01-01;Kategori;100.0;UTGIFT";

        //Act:
        String actual = filHanterare.tillCsvRad(transaktion);

        //Assert:
        assertEquals(expected, actual);
    }

}