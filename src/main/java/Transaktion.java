import java.time.LocalDate;

/**
 * Transaktion är en record så den får equals och hashCode() automatiskt.
 *
 * @param datum
 * @param kategori
 * @param belopp
 * @param typ
 */

public record Transaktion(LocalDate datum, String kategori, double belopp, TransaktionTyp typ) {
}
