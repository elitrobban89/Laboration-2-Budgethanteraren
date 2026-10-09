import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Validerar transaktioner.
 */
public class TransaktionValidator {
    /**
     * parseBelopp gör om text till belopp.
     * @throws InvalidTransactionException
     */
    public static double parseBelopp(String text) throws InvalidTransactionException {
        if (text == null) {
            throw new InvalidTransactionException("Belopp måste vara ifyllt");
        }
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            throw new InvalidTransactionException("Felaktigt format på belopp!");
        }
    }

    /**
     * Gör om text till ett datum i formatet ÅÅÅÅ-MM-DD.
     * @param text datumet som text, t.ex. "2026-10-01"
     * @return datumet som LocalDate
     * @throws InvalidTransactionException om texten saknas eller inte är ett giltigt datum
     */
    public static LocalDate parseDatum(String text) throws InvalidTransactionException {
        if (text == null) {
            throw new InvalidTransactionException("Datum måste anges");
        }
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException e) {
            throw new InvalidTransactionException("Felaktigt datum! Ange datum som ÅÅÅÅ-MM-DD, t.ex. 2026-10-01");
        }
    }

    /**
     * Kontrollerar att en transaktions belopp och kategori är giltiga.
     *
     * @param belopp   beloppet, måste vara större än 0
     * @param kategori kategorin, får inte vara null eller tom
     * @throws InvalidTransactionException om beloppet är 0 eller negativt, eller om kategorin är tom
     */
    public static void validate(double belopp, String kategori) throws InvalidTransactionException {
        if (!Double.isFinite(belopp) || belopp <= 0) { //Stoppar NaN och Infinity tal.
            throw new InvalidTransactionException("Beloppet måste vara större än 0");
        }
        if (kategori == null || kategori.trim().isEmpty()) {
            throw new InvalidTransactionException("Kategorin måste vara ifylld");
        }
        if (kategori.contains(";")) {
            throw new InvalidTransactionException("Kategorin får inte innehålla semikolon (;)");
        }
    }
}
