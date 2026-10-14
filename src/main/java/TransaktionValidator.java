import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Validerar transaktioner.
 * @NullMarked betyder att inget i klassen får vara null, om det inte står @Nullable.
 */
@NullMarked
public class TransaktionValidator {
    /**
     * parseBelopp gör om text till belopp.
     * @throws InvalidTransactionException
     */
    public static double parseBelopp(@Nullable String text) throws InvalidTransactionException {
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
    public static LocalDate parseDatum(@Nullable String text) throws InvalidTransactionException {
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
     * @param kategori kategorin, får inte vara null, tom eller innehålla semikolon
     * @throws InvalidTransactionException om beloppet är 0, negativt, NaN eller Infinity,
     *                                     om kategorin är tom, eller om kategorin innehåller semikolon (bugg 6)
     */
    public static void validate(double belopp, @Nullable String kategori) throws InvalidTransactionException {
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

    /**
     * Gör om användarens menyval till en TransaktionTyp.
     * Optional används eftersom valet kan vara ogiltigt, då returneras Optional.empty() i stället för null.
     *
     * @param val "1" för INKOMST eller "2" för UTGIFT, får vara null (t.ex. vid Ctrl+D)
     * @return typen i en Optional, eller Optional.empty() om valet är ogiltigt
     */
    public static Optional<TransaktionTyp> tolkaTyp(@Nullable String val) {
        if (val == null) {
            return Optional.empty();
        }
        return switch (val.trim()) { //Tar bort mellanslag så att t.ex. " 1" räknas som 1
            case "1" -> Optional.of(TransaktionTyp.INKOMST);
            case "2" -> Optional.of(TransaktionTyp.UTGIFT);
            default -> Optional.empty();
        };
    }
}
