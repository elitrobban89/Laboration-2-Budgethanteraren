# Laboration 2 – Budgethanteraren (CLI)

En interaktiv konsolapplikation i Java där användaren kan registrera inkomster och utgifter, kategorisera dem, spara/läsa dem från fil och få en sammanställning av sin ekonomi.

Uppgiften examinerar främst **läranderesultat 7**: enhetstester med JUnit 5, felsökning med debugger och loggning samt att åtgärda defekter utifrån testresultat.

## Teknik

- Java 27
- Maven
- JUnit 5 (Jupiter)

## Köra projektet

```bash
# Kör alla tester
mvn test

# Kompilera
mvn compile
```

Applikationen startas från `CliApp` (t.ex. via Run i IntelliJ).

## Projektstruktur

```
src/
├── main/java/
│   ├── CliApp.java                       # Meny och användarinteraktion
│   ├── InvalidTransactionException.java  # Eget checked undantag för ogiltiga transaktioner
│   ├── Repository.java                   # Generisk lagringsklass Repository<T>
│   ├── Transaktion.java                  # record: datum, kategori, belopp, typ
│   ├── TransaktionTyp.java               # enum: INKOMST, UTGIFT
│   └── TransaktionValidator.java         # Parsar och validerar belopp och kategori
└── test/java/
    ├── RepositoryTest.java            # JUnit 5-tester för Repository<T>
    └── TransaktionValidatorTest.java  # JUnit 5-tester för TransaktionValidator
```

## Lösningens uppbyggnad

- **`TransaktionTyp`** – enum med `INKOMST` och `UTGIFT`.
- **`Transaktion`** – ett `record` som håller datum, kategori, belopp och typ. Records är oföränderliga, vilket passar en transaktion som inte ska ändras efter att den skapats.
- **`Repository<T>`** – en egen generisk klass som lagrar objekt i en `ArrayList<T>`:
  - `add(T item)` – lägger till ett objekt.
  - `findAll()` – returnerar en *kopia* av listan så att repot inte kan ändras utifrån.
  - `findWhere(Predicate<T> villkor)` – filtrerar med Stream API och ett lambda-villkor.
- **`InvalidTransactionException`** – eget *checked* undantag (`extends Exception`). Eftersom det är checked tvingar kompilatorn anroparen att hantera felet med `try/catch`.
- **`TransaktionValidator`** – samlar all validering av indata och alla felmeddelanden på ett ställe:
  - `parseBelopp(text)` – gör om text till ett `double`. Kastar `InvalidTransactionException` om texten saknas (`null`) eller inte är ett tal, i stället för att låta `NumberFormatException` nå menyn.
  - `validate(belopp, kategori)` – kastar `InvalidTransactionException` om beloppet är 0 eller negativt, eller om kategorin är tom.

  Valideringen ligger i en egen klass (i stället för i menyn) så att den kan testas med JUnit utan tangentbordsinmatning.
- **`CliApp`** – menyn. Väljer typ och anropar sedan `parseBelopp` och `validate` i ett gemensamt `try/catch`. Vid fel skrivs validatorns meddelande ut med `e.getMessage()` och programmet fortsätter utan att krascha. Kategorin trimmas först efter valideringen, när den säkert inte är `null`.

## Status

### Domänmodell
- [x] `enum TransaktionTyp`
- [x] `record Transaktion`
- [x] Generisk klass `Repository<T>` med `add`, `findAll`, `findWhere`
- [x] Eget undantag `InvalidTransactionException`
- [x] `TransaktionValidator` som kastar `InvalidTransactionException`
- [x] `TransaktionValidator.parseBelopp` – felaktigt format ger `InvalidTransactionException`
- [x] `CliApp` använder `TransaktionValidator` med `try/catch`
- [ ] `FileFormatException` för trasiga rader i filen

### Meny / funktionalitet
- [x] 1. Lägg till transaktion (med validering av indata)
- [x] 2. Visa alla transaktioner
- [ ] 3. Visa saldo och sammanställning per kategori (Stream: `groupingBy`/`summingDouble`)
- [ ] 4. Filtrera/sortera transaktioner (datumintervall, typ)
- [ ] 5. Spara till fil (CSV, try-with-resources)
- [ ] Läsa in transaktioner från fil vid start, hantera saknad/trasig fil utan krasch
- [x] e. Avsluta

### Tester (JUnit 5, Arrange-Act-Assert)
- [x] `RepositoryTest` – 9 tester:
  - [x] lägga till objekt, ordning bevaras, dubbletter tillåts
  - [x] `findAll` på tomt repo, `findAll` returnerar en kopia
  - [x] `findWhere` med matchning, stor/liten bokstav och ingen träff
  - [x] gränsfall: `findWhere` på tomt repo
  - [x] `assertThrows` – resultatet från `findWhere` går inte att ändra
  - [x] generics + gränsvärden – `Repository<Integer>` med värden runt 100
- [ ] `TransaktionValidatorTest` – påbörjad, 3 tester:
  - [x] `parseBelopp` normalfall – `" 100 "` blir `100` (parsning + trim)
  - [x] `parseBelopp` med `null` – ger `InvalidTransactionException`, inte `NullPointerException`
  - [x] `parseBelopp` med bokstäver – `assertThrows` + kontroll av felmeddelandet
  - [ ] `validate` – gränsvärden för belopp (`0`, negativt, `0.01`) och kategori (tom, blank, `null`)
  - [ ] `NaN`/`Infinity` som belopp (förväntas faila först – kandidat till dokumenterad bugg)
- [ ] Testklass för sammanställnings-/beräkningslogiken
- [ ] Tester för fil-I/O (läsa/skriva, trasig rad)

### Loggning
- [ ] Loggning vid felaktig indata och filfel (G)
- [ ] Loggning med flera nivåer – DEBUG/INFO/WARNING/ERROR – konsekvent i hela appen (VG)

### Dokumentation
- [ ] Minst en dokumenterad bugg (se nedan)
- [ ] Reflektion kring generics och Stream API (VG)

## Felsökning – dokumenterad bugg

*Kommer att fyllas i.* Beskriver:
1. **Upptäckt** – hur buggen hittades (failande test, debugger, loggutskrift).
2. **Felsökning** – breakpoints, stegning, loggar.
3. **Åtgärd** – vad som ändrades och hur det verifierades med tester.

## Reflektion: generics och Stream API

### Jämförelse med Laboration 1
I Laboration 1 (Bibliotekshanteraren) fick vi inte använda generics eller collections.
Lagringen byggde därför på vanliga arrayer, vilket gav flera problem:

- **Fast storlek** – arrayen fick en bestämd storlek från början, t.ex. `new Bok[100]`.
  Behövde vi plats för fler fick vi själva skapa en större array och kopiera över allt.
- **Egen räknare** – vi fick hålla reda på hur många platser som faktiskt var använda
  (`antal++`) och se upp med `null` i de tomma platserna.
- **En sökmetod per villkor** – varje sökning (på titel, författare osv.) blev en egen
  metod med en egen for-loop och if-sats.
- **Låst till en typ** – lagringen fungerade bara för böcker. Ville man spara något
  annat fick man skriva en ny klass med nästan samma kod.

### Hur det blev lättare i Laboration 2
- **`ArrayList`** växer automatiskt. `add()` sköter storlek och räknare åt oss,
  och det finns inga `null`-luckor att hålla koll på.
- **Generics (`Repository<T>`)**: samma klass fungerar för vilken typ som helst.
  I appen lagrar den `Transaktion` och i testerna `Integer`, utan en enda rad ny kod.
  Kompilatorn kontrollerar också typen, så det går inte att råka lägga en `String`
  i ett `Repository<Transaktion>`.
- **`Predicate<T>` + Stream API**: i stället för en sökmetod per villkor räcker det
  med `findWhere`. Villkoret skickas in som en lambda, t.ex.
  `repo.findWhere(t -> t.typ() == TransaktionTyp.UTGIFT)`. Nya filter kräver alltså
  ingen ny metod i `Repository`.
- **Mindre kod blir lättare att testa**: eftersom `Repository` är liten och generell
  kunde den testas helt fristående i `RepositoryTest`.

Nackdelen är att mer sker "bakom kulisserna". Med arrayer i Laboration 1 såg man exakt
vad som hände i varje steg, och det gav en bra förståelse för vad `ArrayList` och
streams faktiskt gör åt en.

## Visualisering från Plan mode i Claude

Översikt över uppgiften: arkitektur, meny, G/VG-krav som checklista och förslag på arbetsordning.

[Öppna visualiseringen](https://claude.ai/artifact/Fh8swzWQhvkKQG8c3UYsqf)
