import java.time.LocalDate;

public record Transaktion(LocalDate datum,String kategori,double belopp,TransaktionTyp typ) {
}
