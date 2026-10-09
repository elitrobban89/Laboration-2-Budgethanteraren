/**
 * Har skapat TransaktionFilHanterare för att hantera transaktioner från filer.
 * Den läser in transaktioner och sparar dem till en CSV fil.
 * Iom att vår record ser ut så bör vi ha filformatet: (datum; kategori; belopp; typ) så att det blir samma ordning i utdatafilen.
 */
public class TransaktionFilHanterare {

    /**
     * Metod för att omvandla en transaktion till en rad i CSV format. Men ingenting skrivs till disk.
     * Vi delar upp stegen för att testerna ska fungera.
     */ public String tillCsvRad(Transaktion t) {
         return t.datum() + ";" + t.kategori() + ";" + t.belopp() + ";" + t.typ();
    }
}
