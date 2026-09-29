import java.time.LocalDate;

public class CliApp {

    /**
     * Skapa fält (variabel) för Repository<Transaktion>
     */
    private static final Repository<Transaktion> repository = new Repository<>();

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
                    //metod
                    break;
                case "3":
                    //metod
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
     *
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
        if (beloppStr == null) {
            return;
        }
        double belopp;

        /**
         * Försöker parsa strängen till ett double värde meddelar om det inte lyckas
         */
        try {
            belopp = Double.parseDouble(beloppStr.trim());
        } catch (NumberFormatException e) {
            IO.println("Felaktigt format på belopp! Mata in ett giltigt tal: ");
            return;
        }
        if (belopp <= 0) {
            IO.println("Beloppet måste vara större än 0.");
            return;
        }
        IO.println("Belopp: " + belopp + " kr");

        //Inläsning och validering av kategori

        String kategori = IO.readln("Ange kategori: ");
        if (kategori == null || kategori.trim().isEmpty()) {
            IO.println("Kategori får inte vara tom!");
            return;
        }
        kategori = kategori.trim();
        IO.println("Kategori: " + kategori);

        //Datumhantering lägger till dagens datum
        LocalDate datum = LocalDate.now();
        IO.println("Datum: " + datum);

        //Skapa transaktionen
        Transaktion transaktion = new Transaktion(datum, kategori, belopp,typ);
        IO.println("Transaktionen har skapats: " + transaktion);

        repository.add(transaktion); //Lägger till transaktionen i samlingen.
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