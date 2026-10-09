import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Har skapat TransaktionFilHanterare för att hantera transaktioner från filer.
 * Den läser in transaktioner och sparar dem till en CSV fil.
 * Iom att vår record ser ut så bör vi ha filformatet: (datum; kategori; belopp; typ) så att det blir samma ordning i utdatafilen.
 */
public class TransaktionFilHanterare {

    /**
     * Metod för att omvandla en transaktion till en rad i CSV format. Men ingenting skrivs till disk.
     * Vi delar upp stegen för att testerna ska fungera.
     */
    public String tillCsvRad(Transaktion t) {
        return t.datum() + ";" + t.kategori() + ";" + t.belopp() + ";" + t.typ();
    }

    /**
     * Metod för att omvandla en rad i CSV format till en transaktion.
     * Vi delar upp stegen för att testerna ska fungera.
     */
    public Transaktion franCsvRad(String rad) throws FileFormatException {
        String[] delar = rad.split(";");

        if (delar.length != 4) {
            throw new FileFormatException("Felaktig rad i filen: " + rad);
        }

        try {
            LocalDate datum = LocalDate.parse(delar[0]);
            String kategori = delar[1];
            double belopp = Double.parseDouble(delar[2]);
            TransaktionTyp typ = TransaktionTyp.valueOf(delar[3]);
            return new Transaktion(datum, kategori, belopp, typ);
        } catch (DateTimeParseException | IllegalArgumentException e) { //Fånga det ena eller det andra. NumberFormatException är en underklass till IllegalArgumentException, så den fångas av den.
            throw new FileFormatException("Felaktig rad i filen: " + rad);
        }
    }
}
