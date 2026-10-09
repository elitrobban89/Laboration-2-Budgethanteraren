import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;
import java.util.List;
import java.util.logging.Logger;

public class CliApp {

    /**
     * Repot där alla transaktioner lagras, och tjänsten som räknar på samma repo.
     */
    private static final Repository<Transaktion> repository = new Repository<>();
    private static final BudgetService budgetService = new BudgetService(repository);
    private static final Path FIL = Path.of("transaktioner.csv"); //Spara till en fil med defaultnamnet
    private static final TransaktionFilHanterare filHanterare = new TransaktionFilHanterare();
    private static final Logger logger = Logger.getLogger(CliApp.class.getName());
    private static boolean filenLastesIn = false; //Blir true om filen har kunnat läsas in

    static void main() {
        boolean running = true;
        lasFranFil(); //Läser in sparade transaktioner från filen när programmet startar. Så det inte försvinner mellan programomstarter.
        do {
            printMenu();
            String val = IO.readln("Välj Menyalternativ: ");
            if (val == null) {
                IO.println("Felaktig input, programmet avslutas");
                running = false;
                continue;
            }
            val = val.trim(); //Vi trimmar även val så man inte får in ett blanksteg i slutet av menyalternativet
            switch (val) {
                case "1":
                    skapaTransaktion();
                    break;
                case "2":
                    visaAllaTransaktioner();
                    break;
                case "3":
                    visaSaldoochSammanstallning();
                    break;
                case "4":
                    filtreraTransaktioner();
                    break;
                case "5":
                    sparaTillFil();
                    break;
                case "e":
                    sparaTillFil(); //Vid avslut ska vi spara transaktionerna till filen så att de inte försvinner
                    IO.println("Avslutar programmet");
                    running = false; //Avsluta programmet
                    break;
                default:
                    IO.println("Ogiltigt val: '" + val + "'. Välj 1-5 eller e. för att avsluta");
            }
        } while (running);
    }

    /**
     * Menyval 1: Skapa transaktion med enum TransaktionTyp
     * Metoden används bara inom klassen
     * <p>
     * Sedan läggs transaktionen till i repositoryt.
     *
     */
    private static void skapaTransaktion() {
        IO.println("--- 1. Lägg till transaktion ---");
        String typVal = IO.readln("Välj 1 för INKOMST, 2 för UTGIFT): ");

        if (typVal == null) {
            return;
        }
        TransaktionTyp typ;
        if (typVal.equals("1")) {
            typ = TransaktionTyp.INKOMST;
        } else if (typVal.equals("2")) {
            typ = TransaktionTyp.UTGIFT;
        } else {
            IO.println("Ogiltigt val. Mata in 1 för INKOMST eller 2 för UTGIFT: ");
            return;
        }
        IO.println("Du valde: " + typ);

        String beloppStr = IO.readln("Ange belopp: ");
        double belopp;
        String kategori;

        //Validering sker i TransaktionValidator. Fel kastas som InvalidTransactionException och skrivs ut här.

        try {
            belopp = TransaktionValidator.parseBelopp(beloppStr);
            kategori = IO.readln("Ange kategori: ");
            TransaktionValidator.validate(belopp, kategori);
        } catch (InvalidTransactionException e) {
            IO.println(e.getMessage());
            logger.warning("Ogiltig transaktion: " + e.getMessage());
            return;
        }
        kategori = kategori.trim();

        IO.println("Belopp: " + belopp);
        IO.println("Kategori: " + kategori);

        //Datumhantering lägger till dagens datum
        LocalDate datum = LocalDate.now();
        IO.println("Datum: " + datum);

        //Skapa transaktionen
        Transaktion transaktion = new Transaktion(datum, kategori, belopp, typ);
        IO.println("Transaktionen har skapats: " + transaktion);

        repository.add(transaktion); //Lägger till transaktionen i samlingen.
    }

    /**
     * Menyval 2 Visa alla transaktioner
     * Vi anropar inte findAll varje gång vi loopar pga att det är en kostsam operation
     */
    private static void visaAllaTransaktioner() {
        IO.println("--- 2. Visa alla transaktioner ---");
        var transaktioner = repository.findAll();
        if (transaktioner.isEmpty()) {
            IO.println("Inga transaktioner att visa");
        } else {
            for (Transaktion transaktion : transaktioner) {
                IO.println(transaktion);
            }
        }
    }

    /**
     * Menyval 3 Visa saldo och sammanställning per kategori
     *
     */
    private static void visaSaldoochSammanstallning() {
        //Saldo:
        IO.println("--- 3. Visa saldo och sammanställning per kategori ---");
        double saldo = budgetService.saldo();
        IO.println("Saldo: " + saldo + " kr");
        IO.println("");

        //Summa per kategori:
        IO.println("Summa per kategori: ");
        Map<String, Double> sammanstallning = budgetService.summaPerKategori();
        if (sammanstallning.isEmpty()) { //Per kategori om inga transaktioner finns.
            IO.println("Inga transaktioner att visa");
        } else {
            for (Map.Entry<String, Double> entry : sammanstallning.entrySet()) {
                IO.println(entry.getKey() + ": " + entry.getValue() + " kr");
            }
        }
    }

    /**
     * Menyval 4 Filtrera på datum eller typ
     *
     */
    private static void filtreraTransaktioner() {
        IO.println("--- 4.Filtrera transaktioner ---");
        String val = IO.readln("Välj filtrering: \n1. Datum\n2. Typ\n3 Alla, sorterade på datum\nVälj: ");
        if (val == null) {
            IO.println("Ogiltigt val");
            return;
        }
        if (val.equals("1")) {
            visaFiltreratPaDatum();
        } else if (val.equals("2")) {
            visaFiltreratPaTyp();
        } else if(val.equals("3")) {
            skrivUt(budgetService.sorteraPaDatum());
        } else {
            IO.println("Ogiltigt val");
        }
    }

    /**
     * Menyval 4.2 Filtrerat på typ
     *
     */
    private static void visaFiltreratPaTyp() {
        String typVal = IO.readln("Välj 1 för INKOMST, 2 för UTGIFT: ");
        TransaktionTyp typ;
        if ("1".equals(typVal)) {
            typ = TransaktionTyp.INKOMST;
        } else if ("2".equals(typVal)) {
            typ = TransaktionTyp.UTGIFT;
        } else {
            IO.println("Ogiltigt val");
            return;
        }
        skrivUt(budgetService.filtreraTyp(typ));
    }
    /**
     * Menyval 4.1 Frågar efter ett datumintervall och visar transaktionerna inom det.
     */
    private static void visaFiltreratPaDatum() {
        String startDatumText = IO.readln("Ange startdatum (ÅÅÅÅ-MM-DD): ");
        String slutDatumText = IO.readln("Ange slutdatum (ÅÅÅÅ-MM-DD): ");
        try {
            LocalDate startDatum = TransaktionValidator.parseDatum(startDatumText);
            LocalDate slutDatum = TransaktionValidator.parseDatum(slutDatumText);
            if (startDatum.isAfter(slutDatum)) {
                IO.println("Startdatum kan inte vara efter slutdatum");
                logger.warning("Ogiltigt datumintervall: Startdatum efter slutdatum: " + startDatum +" är efter " + slutDatum);
                return;
            }
            skrivUt(budgetService.filtreraDatum(startDatum, slutDatum));
        } catch (InvalidTransactionException e) {
            IO.println(e.getMessage());
            logger.warning("Ogiltigt datumintervall: " + e.getMessage());
        }
    }

    private static void skrivUt(List<Transaktion> transaktioner) {
        if (transaktioner == null || transaktioner.isEmpty()) {
            IO.println("Inga transaktioner matchade filtret");
            return;
        }
        for (Transaktion transaktion : transaktioner) {
            IO.println(transaktion);
        }
    }

    /**
     * Menyval 5 Spara till fil
     * Om det misslyckas skrivs ett felmeddelande ut
     */
    private static void sparaTillFil() {
        if (!filenLastesIn) {
            IO.println("Sparar inte: filen kunde inte läsas vid start, så den skrivs inte över. ");
            logger.severe("Sparning stoppad: " + FIL + " kunde inte läsas in vid start, men den skrivs inte över.");
            return;
        }
        try {
            filHanterare.spara(repository.findAll(),FIL);
            IO.println("Transaktionerna sparades till fil: " + FIL);
            logger.info("Sparade " + repository.findAll().size() + " transaktioner till " + FIL);
        } catch (IOException e) {
            IO.println("Det gick inte att spara till fil: " + e.getMessage());
            logger.severe("Kunde inte spara till " + FIL + ": " + e.getMessage());
        }
    }
    /**
     * Läser in sparade transaktioner från filen när programmet startar.
     * Saknas filen skapas en tom fil. Om läsningen misslyckas startar programmet med ett tomt repo.
     */
    private static void lasFranFil() {
        try {
            List<Transaktion> sparade = filHanterare.las(FIL);
            for (Transaktion t : sparade) {
                repository.add(t);
            }
            filenLastesIn = true;
            IO.println(sparade.size() + " transaktioner lästes in från " + FIL);
            logger.info("Läste in " + sparade.size() + " transaktioner från " + FIL);
        } catch (IOException e) {
            IO.println("Kunde inte läsa filen: " + e.getMessage());
            logger.severe("Kunde inte läsa " + FIL + ": " + e.getMessage());
        }
    }

    public static void printMenu() {
        String menyText = """
                Budgethanteraren
                ==================
                1. Lägg till transaktion
                2. Visa alla transaktioner
                3. Visa saldo och sammanställning per kategori
                4. Filtrera transaktioner
                5. Spara till fil
                e. Avsluta
                """;
        IO.println(menyText);
    }
}