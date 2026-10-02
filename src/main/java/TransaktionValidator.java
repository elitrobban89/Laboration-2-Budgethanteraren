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
     * Kontrollerar att en transaktions belopp och kategori är giltiga.
     *
     * @param belopp   beloppet, måste vara större än 0
     * @param kategori kategorin, får inte vara null eller tom
     * @throws InvalidTransactionException om beloppet är 0 eller negativt, eller om kategorin är tom
     */
    public static void validate(double belopp, String kategori) throws InvalidTransactionException {
        if (belopp <= 0) {
            throw new InvalidTransactionException("Beloppet måste vara större än 0");
        }
        if (kategori == null || kategori.trim().isEmpty()) {
            throw new InvalidTransactionException("Kategori måste vara ifylld");
        }
    }
}
