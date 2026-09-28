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
                    //metod
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
