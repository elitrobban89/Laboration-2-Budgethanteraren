import java.time.LocalDate;

public class CliApp {

    /**
     * Repot där alla transaktioner lagras, och tjänsten som räknar på samma repo.
     */
    private static final Repository<Transaktion> repository = new Repository<>();
    private static final BudgetService budgetService = new BudgetService(repository);

    static void main() {
        boolean running = true;
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
                    //metod
                    break;
                case "5":
                    //metod
                    break;
                case "e":
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
     * Sedan lägg till transaktionen i samlingen.
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

        //CliApp använder TransaktionValidator (parseBelopp + validate) med try/catch. Alla felmeddelanden ligger i validatorn.
        //Parsar och validerar belopp och kategori via TransaktionValidator
        //Vi bytte ut if satserna för att använda TransaktionValidator.validate

        try {
            belopp = TransaktionValidator.parseBelopp(beloppStr);
            kategori = IO.readln("Ange kategori: ");
            TransaktionValidator.validate(belopp,kategori);
        } catch (InvalidTransactionException e) {
            IO.println(e.getMessage());
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
        IO.println("--- 3. Visa saldo och sammanställning per kategori ---");
        double saldo = budgetService.saldo();
        IO.println("Saldo: " + saldo + " kr");
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