import java.time.LocalDate;

/**
 * Transaktion är en record så den får equals och hashCode() automatiskt.
 *
 * @param datum    dagen transaktionen gjordes
 * @param kategori t.ex. Mat eller Lön, får inte innehålla semikolon
 * @param belopp   alltid positivt, typen avgör om det är plus eller minus
 * @param typ      INKOMST eller UTGIFT
 */

public record Transaktion(LocalDate datum, String kategori, double belopp, TransaktionTyp typ) {
}
