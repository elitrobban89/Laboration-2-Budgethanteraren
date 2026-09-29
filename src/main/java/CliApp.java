import java.awt.print.Book;

public class CliApp {

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
     */
    private static void skapaTransaktion(enum TransaktionTyp typ) {
        enum inkomst;
        enum utgift;

        if (inkomst == null || utgift == null) {
            IO.println("Felaktig input, programmet avslutas");
            return;
        }
        if (inkomst <0 || utgift < 0) {
            IO.println("Värdet kan inte vara mindre än 0. Mata in rätt värde: ");
            return;
        }

        /** Använder trimning
         * Skriver användaren av vana ett mellanslag efter inkomst eller utgift, och felmeddelandet blir
         *
         */
        titel = titel.trim();
        forfattare = forfattare.trim();
        isbn = isbn.trim();

        if (titel.isBlank() || forfattare.isBlank() || isbn.isBlank()) {
            IO.println("Tom input, alla fält måste fyllas i.");
            return;
        }
        Book bok = new Book(titel, forfattare, isbn);
        if (lib.laggTillBok(bok)) {
            IO.println("Bok " + bok.titel() + " har lagts till");
        } else {
            IO.println("Bok med ISBN " + isbn + " finns redan.");
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
