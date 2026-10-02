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
│   ├── BudgetService.java                # Beräkningar: saldo och summa per kategori
│   ├── CliApp.java                       # Meny och användarinteraktion
│   ├── InvalidTransactionException.java  # Eget checked undantag för ogiltiga transaktioner
│   ├── Repository.java                   # Generisk lagringsklass Repository<T>
│   ├── Transaktion.java                  # record: datum, kategori, belopp, typ
│   ├── TransaktionTyp.java               # enum: INKOMST, UTGIFT
│   └── TransaktionValidator.java         # Parsar och validerar belopp och kategori
└── test/java/
    ├── BudgetServiceTest.java         # JUnit 5-tester för beräkningarna i BudgetService
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
  - `validate(belopp, kategori)` – kastar `InvalidTransactionException` om beloppet är 0, negativt, `NaN` eller `Infinity`, eller om kategorin är tom.

  Valideringen ligger i en egen klass (i stället för i menyn) så att den kan testas med JUnit utan tangentbordsinmatning.
- **`BudgetService`** – räknar på transaktionerna i repot. Den läser inte från tangentbordet och skriver inte ut något, så den kan testas med JUnit.
  - Repot skickas in via konstruktorn (`new BudgetService(repository)`), så att tjänsten räknar på samma repo som menyn lägger till transaktioner i – och så att tester kan skicka in ett eget repo.
  - `saldo()` – inkomster minus utgifter med Stream API: `mapToDouble` gör varje inkomst till `+belopp` och varje utgift till `-belopp`, och `sum()` summerar. Beloppen sparas alltid positiva; det är typen (`INKOMST`/`UTGIFT`) som avgör tecknet. Inga transaktioner ger `0.0`.
  - `summaPerKategori()` – returnerar en `Map<String, Double>` med kategorin som nyckel och summan som värde. Byggs med `Collectors.groupingBy(t -> t.kategori(), Collectors.summingDouble(t -> t.belopp()))`: transaktioner med samma kategori hamnar i samma grupp och deras belopp summeras. Här räknas inte inkomst minus utgift – varje kategori summeras för sig. Inga transaktioner ger en tom `Map`.
- **`CliApp`** – menyn. Väljer typ och anropar sedan `parseBelopp` och `validate` i ett gemensamt `try/catch`. Vid fel skrivs validatorns meddelande ut med `e.getMessage()` och programmet fortsätter utan att krascha. Kategorin trimmas först efter valideringen, när den säkert inte är `null`. Menyval 3 hämtar saldot och summan per kategori från `BudgetService` och skriver ut dem; finns inga transaktioner visas ett meddelande i stället för en tom lista.

Exempel på menyval 3:
```
Saldo: 16707.5 kr

Summa per kategori:
Mat: 1092.5 kr
Hyra: 7200.0 kr
Lön: 25000.0 kr
```

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
- [x] 3. Visa saldo och sammanställning per kategori
  - [x] Saldo (inkomster − utgifter) via `BudgetService.saldo()`
  - [x] Summa per kategori via `BudgetService.summaPerKategori()` (Stream: `groupingBy`/`summingDouble`)
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
- [x] `TransaktionValidatorTest` – 11 tester:
  - [x] `parseBelopp` normalfall – `" 100 "` blir `100` (parsning + trim)
  - [x] `parseBelopp` med `null` – ger `InvalidTransactionException`, inte `NullPointerException`
  - [x] `parseBelopp` med bokstäver – `assertThrows` + kontroll av felmeddelandet
  - [x] `validate` gränsvärden för belopp – `0` (på gränsen, kontroll av felmeddelandet), `-5`, och `0.01` (minsta giltiga, `assertDoesNotThrow`)
  - [x] `validate` kategori – tom (kontroll av felmeddelandet), bara mellanslag, `null`
  - [x] `NaN`/`Infinity` som belopp – avslöjade en bugg som nu är åtgärdad (se *Felsökning* nedan)
- [ ] `BudgetServiceTest` – påbörjad, 2 tester. Varje test bygger sitt eget repo i Arrange och skickar in det i `BudgetService`:
  - [x] `saldo()` gränsfall – inga transaktioner ger `0.0`
  - [x] `saldo()` inkomster minus utgifter – Lön 25000, Mat 842.50, Hyra 7200 ger `16957.50` (förväntat värde uträknat för hand)
  - [ ] `summaPerKategori()` – samma kategori slås ihop, flera kategorier
  - [ ] `summaPerKategori()` gränsfall – inga transaktioner ger tom `Map`
- [ ] Tester för fil-I/O (läsa/skriva, trasig rad)

### Loggning
- [ ] Loggning vid felaktig indata och filfel (G)
- [ ] Loggning med flera nivåer – DEBUG/INFO/WARNING/ERROR – konsekvent i hela appen (VG)

### Dokumentation
- [x] Minst en dokumenterad bugg (se nedan)
- [ ] Reflektion kring generics och Stream API (VG)

## Felsökning – dokumenterad bugg

### Bugg: `NaN` och `Infinity` godkändes som belopp

**Symptom:** Om användaren skrev `NaN` eller `Infinity` som belopp (eller ett jättestort tal som `1e400`)
skapades en transaktion utan felmeddelande:

```
Ange belopp: NaN
Ange kategori: Mat
Transaktionen har skapats: Transaktion[datum=2026-10-02, kategori=Mat, belopp=NaN, typ=UTGIFT]
```

Ett sådant belopp förstör alla senare beräkningar – t.ex. blir `25000 + NaN = NaN`, så hela saldot blir `NaN`.

#### 1. Upptäckt – failande test
I `TransaktionValidatorTest` skrevs två tester som förväntar sig att `validate` ska kasta
`InvalidTransactionException` för ogiltiga belopp:

```java
assertThrows(InvalidTransactionException.class,
        () -> TransaktionValidator.validate(Double.NaN, "Mat"));
assertThrows(InvalidTransactionException.class,
        () -> TransaktionValidator.validate(Double.POSITIVE_INFINITY, "Mat"));
```

Båda testerna blev röda:

```
testValidate_beloppNaN        Expected InvalidTransactionException to be thrown, but nothing was thrown.
testValidate_beloppOandligt   Expected InvalidTransactionException to be thrown, but nothing was thrown.
```

#### 2. Felsökning – debugger
- Breakpoint sattes på raden `if (belopp <= 0)` i `TransaktionValidator.validate`.
- `testValidate_beloppNaN` kördes i **Debug**-läge. I panelen *Variables* syntes `belopp = NaN`.
- Med *Evaluate Expression* (Alt+F8) utvärderades `belopp <= 0` till **`false`**.
- Stegning med *Step Over* (F8) visade att koden hoppade förbi `throw` och lämnade metoden utan undantag.

**Orsak:**
- `Double.parseDouble` godkänner texterna `"NaN"`, `"Infinity"` och `"-Infinity"`, och ett tal som är
  för stort för en `double` (t.ex. `"1e400"`) blir `Infinity`. Felet fångas alltså inte av `NumberFormatException`.
- `NaN` (Not a Number) ger **`false` i alla jämförelser** – även `NaN <= 0`, `NaN > 0` och till och med
  `NaN == NaN`. Därför slank det igenom villkoret `belopp <= 0`.
- `Infinity` är större än 0, så det klarade villkoret på riktigt – men ett oändligt belopp är ändå ogiltigt.

#### 3. Åtgärd
Villkoret i `TransaktionValidator.validate` kompletterades med `Double.isFinite`, som returnerar
`false` för både `NaN` och `Infinity`:

```java
// Före
if (belopp <= 0) {

// Efter
if (!Double.isFinite(belopp) || belopp <= 0) {
```

`NaN` kan inte fångas med en jämförelse som `belopp == Double.NaN` (den är alltid `false`),
därför används metoden `Double.isFinite`.

**Verifiering:**
- `testValidate_beloppNaN` och `testValidate_beloppOandligt` blev gröna, och alla 20 tester går igenom (`mvn test`).
- Appen kördes igen med `NaN` och `1e400` som belopp – båda avvisas nu med felmeddelandet
  och ingen transaktion skapas:

```
Ange belopp: NaN
Ange kategori: Mat
Beloppet måste vara större än 0
```

**Programmet kraschar inte – men transaktionen skapas inte.**
`validate` kastar `InvalidTransactionException`, som fångas i `CliApp`. Där skrivs felmeddelandet ut och
`return` avbryter `skapaTransaktion()` innan `new Transaktion(...)` och `repository.add(...)` körs.
Menyn visas sedan igen och användaren kan välja menyval 1 på nytt och ange ett giltigt belopp:

```
Ange belopp: NaN     → Beloppet måste vara större än 0
Välj Menyalternativ: 1
Ange belopp: 1e400   → Beloppet måste vara större än 0
Välj Menyalternativ: 2 → Inga transaktioner att visa
Välj Menyalternativ: e → Avslutar programmet
```

Testerna ligger kvar som **regressionstester** – om någon senare tar bort `isFinite`-kontrollen blir de röda igen.

**Lärdom:** testa inte bara "vanliga" felaktiga värden (0, negativt, bokstäver) utan även
specialvärden som datatypen själv tillåter – för `double` är det `NaN` och `Infinity`.

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
