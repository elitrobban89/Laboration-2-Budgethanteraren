import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Har skapat TransaktionFilHanterare för att hantera transaktioner från filer.
 * Den läser in transaktioner och sparar dem till en CSV fil.
 * Iom att vår record ser ut så bör vi ha filformatet: (datum; kategori; belopp; typ) så att det blir samma ordning i utdatafilen.
 */
public class TransaktionFilHanterare {
    private static final Logger logger = Logger.getLogger(TransaktionFilHanterare.class.getName()); //Skapa loggerobjekt

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
        } catch (DateTimeParseException |
                 IllegalArgumentException e) { //Fånga det ena eller det andra. NumberFormatException är en underklass till IllegalArgumentException, så den fångas av den.
            throw new FileFormatException("Felaktig rad i filen: " + rad);
        }
    }

    /**
     * Metod för att spara en lista med transaktioner till en fil i CSV-format.
     *
     * @param transaktioner Lista med transaktioner att spara.
     * @param fil           Filen där transaktionerna ska sparas.
     * @throws IOException Om det uppstår ett fel vid skrivning till filen.
     */
    public void spara(List<Transaktion> transaktioner, Path fil) throws IOException { //Path fil säger var filen är någonstans
        try (BufferedWriter writer = Files.newBufferedWriter(fil)) { //Try för att writer ska stängas automatiskt när vi är färdiga med att använda den
            for (Transaktion t : transaktioner) { //Går igenom varje transaktion i listan
                writer.write(tillCsvRad(t));
                writer.newLine();
                logger.fine("Skrev rad: " + tillCsvRad(t)); //Loggar varje rad som skrivs till filen
            }
        }
    }

    public List<Transaktion> las(Path fil) throws IOException {
        List<Transaktion> transaktioner = new ArrayList<>();
        if (!Files.exists(fil)) { //Om inte filen finns så ska den skapas sen hoppa ur snurran utan att krascha programmet
            Files.createFile(fil);
            return transaktioner;
        }
        try (BufferedReader reader = Files.newBufferedReader(fil)) {
            String rad;
            while ((rad = reader.readLine()) != null) {
                if (!rad.isBlank()) {
                    try {
                        transaktioner.add(franCsvRad(rad));
                        logger.fine("Läste rad: " + rad); //Loggar varje rad som läses från filen
                    } catch (FileFormatException e) {
                        logger.warning("Hoppar över trasig rad: " + e.getMessage()); //Loggern är till för utvecklaren ersatte IO.println. CliApp pratar med användaren.
                    }
                }
            }
        }
        return transaktioner;
    }
}