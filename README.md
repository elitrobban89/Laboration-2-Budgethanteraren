# Laboration 2 – Budgethanteraren (CLI)

En interaktiv konsolapplikation i Java där användaren kan registrera inkomster och utgifter, kategorisera dem, spara/läsa dem från fil och få en sammanställning av sin ekonomi.

Uppgiften examinerar främst **läranderesultat 7**: enhetstester med JUnit 5, felsökning med debugger och loggning samt att åtgärda defekter utifrån testresultat.

## Teknik

- Java 27
- Maven
- JUnit 6 (Jupiter, 6.1.3) – samma API som JUnit 5, så testerna skrivs på samma sätt

## Köra projektet

```bash
# Kör alla tester
mvn test

# Kompilera
mvn compile
```

Applikationen startas från `CliApp` (t.ex. via Run i IntelliJ). Körkonfigurationen i `.run/CliApp.run.xml`
följer med repot och sätter `-Dstdin.encoding=UTF-8`, så att å, ä och ö som skrivs i IntelliJ:s konsol
läses in rätt (se *Bugg 5* nedan). Körs appen på annat sätt utan konsol, t.ex. med omdirigerad inmatning,
behöver samma flagga anges: `java -Dstdin.encoding=UTF-8 ...`.

Transaktionerna sparas i `transaktioner.csv` i den mapp programmet körs från. Filen skapas automatiskt
vid första start och ligger i `.gitignore`.

## Projektstruktur

```
.run/
└── CliApp.run.xml                        # Delad körkonfiguration för IntelliJ (-Dstdin.encoding=UTF-8)
src/
├── main/java/
│   ├── BudgetService.java                # Beräkningar: saldo, summa per kategori, filtrering, sortering
│   ├── CliApp.java                       # Meny och användarinteraktion
│   ├── FileFormatException.java          # Eget checked undantag för trasiga rader i CSV-filen
│   ├── InvalidTransactionException.java  # Eget checked undantag för ogiltiga transaktioner
│   ├── Repository.java                   # Generisk lagringsklass Repository<T>
│   ├── Transaktion.java                  # record: datum, kategori, belopp, typ
│   ├── TransaktionFilHanterare.java      # Översätter transaktion ↔ CSV-rad (läser/sparar fil)
│   ├── TransaktionTyp.java               # enum: INKOMST, UTGIFT
│   └── TransaktionValidator.java         # Parsar och validerar belopp och kategori
└── test/java/
    ├── BudgetServiceTest.java            # Tester för beräkningarna i BudgetService
    ├── RepositoryTest.java               # Tester för Repository<T>
    ├── TransaktionFilHanterareTest.java  # Tester för CSV-formatet, spara/läsa fil och trasiga rader
    └── TransaktionValidatorTest.java     # Tester för TransaktionValidator
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
  - `parseDatum(text)` – gör om text i formatet `ÅÅÅÅ-MM-DD` till ett `LocalDate` med `LocalDate.parse`. Fångar `DateTimeParseException` (t.ex. `abc`, `2026-13-45` eller fel format) och kastar `InvalidTransactionException` med ett svenskt meddelande – samma mönster som `parseBelopp`.
  - `validate(belopp, kategori)` – kastar `InvalidTransactionException` om beloppet är 0, negativt, `NaN` eller `Infinity`, eller om kategorin är tom.

  Valideringen ligger i en egen klass (i stället för i menyn) så att den kan testas med JUnit utan tangentbordsinmatning.
- **`FileFormatException`** – eget *checked* undantag för en trasig rad i CSV-filen. Checked med flit: kompilatorn tvingar inläsningen att fånga felet, så att en trasig rad kan loggas och hoppas över i stället för att krascha programmet.
- **`TransaktionFilHanterare`** – översätter mellan transaktioner och rader i CSV-filen. Formatet följer fälten i recorden: `datum;kategori;belopp;typ`.
  - `tillCsvRad(t)` – bygger raden med strängkonkatenering. `String.format("%.2f")` används inte, eftersom svensk locale då skriver decimalkomma (`842,50`) som `Double.parseDouble` inte kan läsa tillbaka.
  - `franCsvRad(rad)` – delar raden med `split(";")`, kontrollerar att det är exakt 4 fält och parsar dem med `LocalDate.parse`, `Double.parseDouble` och `TransaktionTyp.valueOf`. Alla parsningsfel (`DateTimeParseException`, `IllegalArgumentException`) blir `FileFormatException`.
  - `spara(transaktioner, fil)` – skriver en CSV-rad per transaktion med `Files.newBufferedWriter` i **try-with-resources**, så att filen alltid stängs (även vid fel) och inget blir kvar i bufferten. Filen skrivs över. `IOException` skickas vidare till anroparen.
  - `las(fil)` – läser filen rad för rad med `Files.newBufferedReader` i try-with-resources (`readLine()` ger `null` när filen är slut).
    - **Saknas filen** skapas en ny tom fil med `Files.createFile` och en tom lista returneras – programmet kraschar inte vid första start.
    - **Tomma rader** hoppas över (`isBlank()`).
    - **Trasiga rader** hoppas över: `try/catch (FileFormatException)` ligger *inne i* loopen, så en trasig rad stoppar bara sig själv och läsningen fortsätter med nästa rad. Raden loggas som `WARNING` med felmeddelandet från `FileFormatException`.
  - Översättningen (`tillCsvRad`/`franCsvRad`) är skild från själva filläsningen, så att formatet kan testas utan att skapa filer.
  - Filen skickas in som en `Path` till `spara` och `las` i stället för att stå i klassen. Därför kan appen använda `transaktioner.csv` medan testerna använder en tillfällig fil.
- **`BudgetService`** – räknar på transaktionerna i repot. Den läser inte från tangentbordet och skriver inte ut något, så den kan testas med JUnit.
  - Repot skickas in via konstruktorn (`new BudgetService(repository)`), så att tjänsten räknar på samma repo som menyn lägger till transaktioner i – och så att tester kan skicka in ett eget repo.
  - `saldo()` – inkomster minus utgifter med Stream API: `mapToDouble` gör varje inkomst till `+belopp` och varje utgift till `-belopp`, och `sum()` summerar. Beloppen sparas alltid positiva; det är typen (`INKOMST`/`UTGIFT`) som avgör tecknet. Inga transaktioner ger `0.0`.
  - `summaPerKategori()` – returnerar en `Map<String, Double>` med kategorin som nyckel och summan som värde. Byggs med `Collectors.groupingBy(t -> t.kategori(), Collectors.summingDouble(t -> t.belopp()))`: transaktioner med samma kategori hamnar i samma grupp och deras belopp summeras. Här räknas inte inkomst minus utgift – varje kategori summeras för sig. Inga transaktioner ger en tom `Map`.
  - `filtreraTyp(typ)` – returnerar bara inkomster eller bara utgifter. Använder `repository.findWhere(t -> t.typ() == typ)`, så villkoret skickas in som en lambda och `Repository` behöver ingen egen metod för typfilter.
  - `filtreraDatum(start, slut)` – returnerar transaktioner inom ett datumintervall, där **båda gränsdagarna räknas med**: `findWhere(t -> !t.datum().isBefore(start) && !t.datum().isAfter(slut))`.
  - `sorteraPaDatum()` – returnerar alla transaktioner sorterade på datum, äldst först: `stream().sorted(Comparator.comparing(t -> t.datum())).toList()`. Skillnaden mot filtrering: **filtrering väljer *vilka*** transaktioner som visas (färre eller lika många), **sortering bestämmer *ordningen*** (alla är kvar). Repot ändras inte – `findAll()` ger en kopia och `sorted()` skapar en ny ström.
- **`CliApp`** – menyn. Väljer typ och anropar sedan `parseBelopp` och `validate` i ett gemensamt `try/catch`. Vid fel skrivs validatorns meddelande ut med `e.getMessage()` och programmet fortsätter utan att krascha. Kategorin trimmas först efter valideringen, när den säkert inte är `null`. Menyval 3 hämtar saldot och summan per kategori från `BudgetService` och skriver ut dem; finns inga transaktioner visas ett meddelande i stället för en tom lista.
  - Menyval 4 visar en undermeny (`1. Datum`, `2. Typ`, `3. Alla, sorterade på datum`). Varje filter har en egen liten metod i `CliApp` (`visaFiltreratPaTyp`, `visaFiltreratPaDatum`) som frågar användaren och sedan anropar motsvarande metod i `BudgetService`. Namnen skiljer sig från `BudgetService`-metoderna med flit: `CliApp` *frågar och visar*, `BudgetService` *räknar*.
  - Hjälpmetoden `skrivUt(List<Transaktion>)` skriver ut resultatet för alla filter, och visar "Inga transaktioner matchade filtret" om listan är tom. Null-kontrollen står först (`transaktioner == null || transaktioner.isEmpty()`) så att `isEmpty()` aldrig anropas på `null`.
  - Typvalet jämförs med `"1".equals(typVal)` i stället för `typVal.equals("1")`, så att `null` (t.ex. Ctrl+D) ger `false` i stället för en `NullPointerException`.
  - Datumfiltret läser in start- och slutdatum, parsar båda med `parseDatum` i ett gemensamt `try/catch`, och kontrollerar att startdatum inte är efter slutdatum innan `filtreraDatum` anropas. Allt som använder de parsade datumen ligger inne i `try`, eftersom variablerna bara finns i det blocket.
  - **Fil:** sökvägen bestäms på ett ställe, konstanten `FIL = Path.of("transaktioner.csv")`.
    - `lasFranFil()` anropas **en gång** i början av `main`, *före* menyloopen. Transaktionerna från filen läggs in i repot med `add`, så menyval 2–4 ser dem direkt.
    - `sparaTillFil()` (menyval 5) sparar `repository.findAll()` till filen. Samma metod anropas också vid `e`, så att inget försvinner om användaren glömmer att spara.
    - Båda fångar `IOException` och skriver ut ett meddelande, så ett filfel kraschar inte programmet. Lyckad läsning/sparning loggas som `INFO`, misslyckad som `SEVERE`.
    - Flaggan `filenLastesIn` blir `true` först när filen har lästs in utan fel. Är den `false` vägrar `sparaTillFil()` att spara, så att en fil som inte gick att läsa aldrig skrivs över med ett tomt repo (se *Bugg 4* nedan).

Exempel på menyval 4 (datum):
```
Ange startdatum (ÅÅÅÅ-MM-DD): 2099-01-01
Ange slutdatum (ÅÅÅÅ-MM-DD): 2026-01-01
Startdatum kan inte vara efter slutdatum

Ange startdatum (ÅÅÅÅ-MM-DD): abc
Ange slutdatum (ÅÅÅÅ-MM-DD): x
Felaktigt datum! Ange datum som ÅÅÅÅ-MM-DD, t.ex. 2026-10-01
```

Exempel på fil-I/O (tre starter efter varandra):
```
# Första start – filen saknas och skapas
0 transaktioner lästes in från transaktioner.csv
...lägger till Lön 25000 och Mat 842.5, väljer e...
Transaktionerna sparades till fil: transaktioner.csv

# transaktioner.csv
2026-10-09;Lön;25000.0;INKOMST
2026-10-09;Mat;842.5;UTGIFT

# Andra start
2 transaktioner lästes in från transaktioner.csv

# Tredje start – raden "hej hopp" har lagts till i filen för hand
Fel vid läsning av filen: Felaktig rad i filen: hej hopp
2 transaktioner lästes in från transaktioner.csv
```

Exempel på menyval 3:
```
Saldo: 16707.5 kr

Summa per kategori:
Mat: 1092.5 kr
Hyra: 7200.0 kr
Lön: 25000.0 kr
```

## Loggning

Appen loggar med **`java.util.logging`**, som är inbyggt i Java – inga extra beroenden i `pom.xml`.
Varje klass som loggar har en egen logger:

```java
private static final Logger logger = Logger.getLogger(CliApp.class.getName());
```

**Två mottagare, två verktyg:**
- `IO.println` är till för **användaren** – vad som hände och vad hen ska göra annorlunda.
- `logger` är till för **utvecklaren** – varje loggrad får automatiskt tidsstämpel, nivå och vilken klass och metod den kom från, vilket gör den användbar vid felsökning.

Därför loggar `TransaktionFilHanterare` (en filklass) i stället för att skriva till användaren – det är `CliApp` som pratar med användaren.

**Nivåer** – `java.util.logging` har andra namn än uppgiftens, men de motsvarar varandra:

| Uppgiften | `java.util.logging` | Används i appen för |
|---|---|---|
| DEBUG | `FINE` | *(återstår)* detaljer under felsökning |
| INFO | `INFO` | normala händelser: filen lästes in / sparades, med antal transaktioner |
| WARNING | `WARNING` | något var fel men programmet hanterar det: ogiltig inmatning, trasig rad i filen |
| ERROR | `SEVERE` | allvarligt fel: filen gick inte att läsa eller spara (data kan gå förlorad) |

En trasig rad är `WARNING` och inte `SEVERE`: raden hoppas över och resten av filen läses in som vanligt.
Ett misslyckat sparande är däremot `SEVERE`, eftersom användarens transaktioner då inte finns kvar efter avslut.

Exempel från en körning (en trasig rad i filen, belopp `abc`, bakvänt datumintervall, avslut):
```
okt. 09, 2026 11:52:39 FM TransaktionFilHanterare las
WARNING: Hoppar över trasig rad: Felaktig rad i filen: hej hopp
okt. 09, 2026 11:52:39 FM CliApp lasFranFil
INFO: Läste in 1 transaktioner från transaktioner.csv
okt. 09, 2026 11:52:39 FM CliApp skapaTransaktion
WARNING: Ogiltig transaktion: Felaktigt format på belopp!
okt. 09, 2026 11:52:39 FM CliApp visaFiltreratPaDatum
WARNING: Ogiltigt datumintervall: Startdatum efter slutdatum: 2099-01-01 är efter 2026-01-01
okt. 09, 2026 11:52:39 FM CliApp sparaTillFil
INFO: Sparade 1 transaktioner till transaktioner.csv
```

## Status

### Domänmodell
- [x] `enum TransaktionTyp`
- [x] `record Transaktion`
- [x] Generisk klass `Repository<T>` med `add`, `findAll`, `findWhere`
- [x] Eget undantag `InvalidTransactionException`
- [x] `TransaktionValidator` som kastar `InvalidTransactionException`
- [x] `TransaktionValidator.parseBelopp` – felaktigt format ger `InvalidTransactionException`
- [x] `TransaktionValidator.parseDatum` – felaktigt datum ger `InvalidTransactionException`
- [x] `CliApp` använder `TransaktionValidator` med `try/catch`
- [x] `FileFormatException` (checked) för trasiga rader i filen
- [x] `TransaktionFilHanterare.tillCsvRad` / `franCsvRad` – transaktion ↔ CSV-rad
- [x] `TransaktionFilHanterare.spara` / `las` – BufferedWriter/BufferedReader i try-with-resources

### Meny / funktionalitet
- [x] 1. Lägg till transaktion (med validering av indata)
- [x] 2. Visa alla transaktioner
- [x] 3. Visa saldo och sammanställning per kategori
  - [x] Saldo (inkomster − utgifter) via `BudgetService.saldo()`
  - [x] Summa per kategori via `BudgetService.summaPerKategori()` (Stream: `groupingBy`/`summingDouble`)
- [x] 4. Filtrera/sortera transaktioner (datumintervall, typ, sortering på datum)
  - [x] `BudgetService.filtreraTyp()` via `findWhere`
  - [x] `BudgetService.filtreraDatum()` via `findWhere` (gränsdagar räknas med)
  - [x] Undermeny i `CliApp` (`1. Datum`, `2. Typ`) och hjälpmetoden `skrivUt` med tomt fall
  - [x] Filtrering på typ kopplad till menyval 4
  - [x] Filtrering på datum kopplad till menyval 4 (inmatning med `parseDatum`, felaktigt datum och bakvänt intervall hanteras utan krasch)
  - [x] Sortering: `BudgetService.sorteraPaDatum()` med `Comparator`, val 3 i undermenyn
- [x] 5. Spara till fil (CSV, try-with-resources)
- [x] Läsa in transaktioner från fil vid start, hantera saknad/trasig fil utan krasch
- [x] e. Avsluta (sparar till fil först)

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
- [x] `BudgetServiceTest` – 7 tester. Varje test bygger sitt eget repo i Arrange och skickar in det i `BudgetService`:
  - [x] `saldo()` gränsfall – inga transaktioner ger `0.0`
  - [x] `saldo()` inkomster minus utgifter – Lön 25000, Mat 842.50, Hyra 7200 ger `16957.50` (förväntat värde uträknat för hand)
  - [x] `summaPerKategori()` – två Mat-transaktioner slås ihop till en post (`size() == 2`), Mat = `1092.50`, Hyra = `7200.0`
  - [x] `summaPerKategori()` gränsfall – inga transaktioner ger tom `Map`, inte `null` (`assertNotNull` + `isEmpty()`)
  - [x] `filtreraTyp()` – bara utgifterna kommer med (`size() == 2` + `allMatch` att alla är `UTGIFT`)
  - [x] `filtreraDatum()` gränsvärden – en transaktion på varje gränsdag, en i mitten och en precis utanför på var sida; 3 av 5 ska med. Avslöjade en bugg (se *Felsökning* nedan)
  - [x] `sorteraPaDatum()` – transaktionerna läggs till i oordning; alla tre finns kvar (`size() == 3`) och ligger i datumordning (`get(0)`, `get(1)`, `get(2)`)

- [x] `TransaktionFilHanterareTest` – 7 tester:
  - [x] `tillCsvRad()` – en transaktion blir `2022-01-01;Kategori;100.0;UTGIFT`
  - [x] `franCsvRad()` normalfall – en hel rad blir rätt `Transaktion` (record-`equals` jämför alla fält)
  - [x] `franCsvRad()` fel antal fält – `assertThrows(FileFormatException.class, ...)`
  - [x] `franCsvRad()` belopp `abc` – `assertThrows`; avslöjade en bugg (se *Felsökning* nedan)
  - [x] `spara()` + `las()` – två transaktioner sparas och läses tillbaka oförändrade, i samma ordning (även `Lön` med ö)
  - [x] `las()` gränsfall: filen saknas – tom lista **och** filen har skapats (`Files.exists`)
  - [x] `las()` trasig rad mitt i filen – hoppas över; raderna före och efter kommer med (`size() == 2`, `get(0)`, `get(1)`)

  Filtesterna använder JUnits **`@TempDir`**: varje test får en ny tillfällig mapp som raderas efteråt, så testerna rör aldrig den riktiga `transaktioner.csv` och lämnar inga filer efter sig. Innehållet i den trasiga filen skrivs direkt i testet med `Files.writeString` och ett textblock (`"""`).

Totalt **34 tester**, alla gröna (`mvn test`). Varje central komponent – `Repository<T>`, `TransaktionValidator`, `BudgetService` och `TransaktionFilHanterare` – har en egen testklass med normalfall, gränsfall och (där det är rimligt) `assertThrows` för egna undantag.

### Loggning
- [x] Loggning vid felaktig indata och filfel (G)
  - [x] `WARNING` i `CliApp` vid ogiltig transaktion, felaktigt datum och bakvänt datumintervall
  - [x] `WARNING` i `TransaktionFilHanterare.las` när en trasig rad hoppas över
  - [x] `INFO` när filen läses in/sparas, `SEVERE` när det misslyckas
- [ ] Loggning med flera nivåer – DEBUG/INFO/WARNING/ERROR – konsekvent i hela appen (VG) – *delvis: INFO, WARNING och SEVERE klara; FINE (DEBUG) och loggkonfiguration återstår*

### Dokumentation
- [x] Minst en dokumenterad bugg (se nedan) – fem buggar dokumenterade
- [ ] Reflektion kring generics och Stream API (VG)

## Felsökning – dokumenterad bugg

### Bugg 1: `NaN` och `Infinity` godkändes som belopp

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

### Bugg 2: datumfiltret missade gränsdagarna

**Symptom:** `filtreraDatum(start, slut)` skulle returnera alla transaktioner i ett intervall, t.ex. hela
oktober (`2026-10-01` – `2026-10-31`). Transaktioner som låg **på** första eller sista dagen kom inte med –
bara de som låg strikt mellan gränserna.

Den första versionen av villkoret var:

```java
return repository.findWhere(t -> t.datum().isBefore(slut) && t.datum().isAfter(start));
```

Koden såg rimlig ut och fungerade för datum mitt i intervallet, så felet syntes inte vid en snabb provkörning.

#### 1. Upptäckt – failande gränsvärdestest
Testet `testFiltreraDatum_gransdagarRaknasMed` skapar fem transaktioner – en precis före intervallet,
en på varje gränsdag, en i mitten och en precis efter – och förväntar sig att tre kommer med:

```
  30 sep  |  1 okt  ...  15 okt  ...  31 okt  |  1 nov
  Före    |  Start       Mitt         Slut    |  Efter
  ✗       |  ✓           ✓            ✓       |  ✗
```

```java
List<Transaktion> oktober = budgetService.filtreraDatum(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));
assertEquals(3, oktober.size());
```

Testet blev rött:

```
testFiltreraDatum_gransdagarRaknasMed  expected: <3> but was: <1>
```

Bara **en** av tre förväntade transaktioner kom med.

#### 2. Felsökning – lambdabreakpoint, stegning och watches
- En **lambdabreakpoint** sattes på lambdan `t -> ...` i `filtreraDatum` (i IntelliJ: klicka i marginalen
  och välj λ i stället för *Line*). Då stannar debuggern **en gång per transaktion** som filtret testar.
- Testet kördes i **Debug**-läge och jag stegade mellan transaktionerna med *Resume* (F9).
- En **watch** lades till för `t.datum().isAfter(start)`, så att värdet visades automatiskt vid varje stopp:

| `t` | Datum | `t.datum().isAfter(start)` |
|---|---|---|
| Före | 2026-09-30 | `false` |
| **Start** | **2026-10-01** | **`false`** ← borde komma med |
| Mitt | 2026-10-15 | `true` |

- På samma sätt gav `t.datum().isBefore(slut)` **`false`** för Slut (`2026-10-31`).

Under felsökningen visade IntelliJ *"repository not available"* inne i lambdan. Det är normalt: en lambda
tar bara med sig de variabler den använder (`t`, `start`, `slut`), inte resten av objektet.

**Orsak:** `isAfter` och `isBefore` i `LocalDate` är **strikta** – samma dag räknas varken som "efter" eller
"före". 1 okt är alltså inte "efter" 1 okt, och 31 okt är inte "före" 31 okt. Därför föll båda gränsdagarna bort.
Det motsvarar skillnaden mellan "över 18 år" och "18 år eller äldre".

#### 3. Åtgärd
Villkoret vändes till "inte före start" och "inte efter slut", vilket betyder "samma dag eller senare"
respektive "samma dag eller tidigare":

```java
// Före
t -> t.datum().isBefore(slut) && t.datum().isAfter(start)

// Efter
t -> !t.datum().isBefore(start) && !t.datum().isAfter(slut)
```

**Verifiering:** `testFiltreraDatum_gransdagarRaknasMed` blev grönt (Start, Mitt och Slut kommer med,
Före och Efter inte), och alla 26 tester går igenom (`mvn test`). Testet ligger kvar som regressionstest.

**Lärdom:** fel vid gränsvärden syns inte om man bara provar med ett värde mitt i intervallet. Ett bra
gränsvärdestest har ett värde **på** varje gräns och ett **precis utanför** varje gräns.

### Bugg 3: trasigt belopp i CSV-filen gav fel undantag (risk för krasch)

**Symptom:** `TransaktionFilHanterare.franCsvRad(rad)` gör om en rad från CSV-filen till en `Transaktion`.
Om raden är trasig ska metoden kasta vårt eget `FileFormatException`, så att inläsningen kan logga raden
och hoppa över den. En rad med rätt antal fält men ett belopp som inte är ett tal, t.ex.
`2022-01-01;Kategori;abc;UTGIFT`, gav i stället Javas eget `NumberFormatException`. Hade en sådan rad
funnits i filen vid start hade programmet kraschat – något uppgiften uttryckligen säger att det inte får göra.

Den första versionen av `catch` var:

```java
} catch (DateTimeParseException e) {
    throw new FileFormatException("Felaktig rad i filen: " + rad);
}
```

#### 1. Upptäckt – failande test
Testet `testFranCsvRad_beloppInteTal_kastarFileFormatException` skickar in en rad där beloppet är `abc`
och förväntar sig `FileFormatException`:

```java
String rad = "2022-01-01;Kategori;abc;UTGIFT";
assertThrows(FileFormatException.class, () -> filHanterare.franCsvRad(rad));
```

Testet blev rött:

```
AssertionFailedError: Unexpected exception type thrown,
Expected :class FileFormatException
Actual   :class java.lang.NumberFormatException
```

Testet committades medan det fortfarande var rött, innan buggen åtgärdades, så att det syns i git-historiken
att det var testet som hittade felet.

#### 2. Felsökning – felmeddelandet från testet
- Felmeddelandet visar **vilket** undantag som kastades i stället: `NumberFormatException`.
- Det kommer från `Double.parseDouble("abc")`, som inte kan göra om texten till ett tal.
- `catch`-blocket fångade bara `DateTimeParseException` (fel på **datumet**). `NumberFormatException` är
  inte en sådan, så den passerade rakt igenom `catch` och kom ut ur metoden.

**Orsak:** `catch` täckte bara ett av de tre fälten som parsas. Samma lucka gällde typen:
`TransaktionTyp.valueOf("KAFFE")` kastar `IllegalArgumentException`, som inte heller fångades.

#### 3. Åtgärd
`catch` fångar nu även `IllegalArgumentException` med en *multi-catch* (`|` = "det ena eller det andra"):

```java
// Före
} catch (DateTimeParseException e) {

// Efter
} catch (DateTimeParseException | IllegalArgumentException e) {
```

`NumberFormatException` är en **underklass** till `IllegalArgumentException`, så den fångas också.
Med en enda extra typ täcks alltså både felaktigt belopp och felaktig typ.

**Verifiering:** `testFranCsvRad_beloppInteTal_kastarFileFormatException` blev grönt, och alla 31 tester
går igenom (`mvn test`). Testet ligger kvar som regressionstest.

**Lärdom:** när en metod parsar flera fält kan varje fält kasta sitt **eget** undantag. Ett test per
sorts trasigt fält visar om `catch` verkligen täcker alla – att ett fel fångas betyder inte att alla gör det.

### Bugg 4: en fil som inte gick att läsa skrevs över med ett tomt repo

**Symptom:** Om `transaktioner.csv` hade sparats med en annan teckenkodning än UTF-8 – t.ex. efter att ha
öppnats och sparats i Excel eller Anteckningar, som ofta sparar å/ä/ö i ANSI (ISO-8859-1) – försvann **all**
data när programmet avslutades. Filen gick från tre transaktioner till 0 byte.

Programmet kraschade inte – felet fångades – men fortsatte som om allt var i ordning:

```java
private static void lasFranFil() {
    try {
        List<Transaktion> sparade = filHanterare.las(FIL);
        ...
    } catch (IOException e) {
        IO.println("Kunde inte läsa filen: " + e.getMessage());   // programmet fortsätter med tomt repo
    }
}
```

#### 1. Upptäckt – kodgranskning och återskapat scenario
Buggen hittades vid en genomgång av flödet *läs vid start → spara vid avslut*: vad händer om läsningen
misslyckas? `CliApp` har inga JUnit-tester (menyn läser från tangentbordet), så scenariot återskapades för hand:

1. En rad med `Lön` lades till i `transaktioner.csv`.
2. Filen konverterades till ISO-8859-1, så att `ö` blev en enda byte som inte är giltig UTF-8.
   IntelliJ varnade *"File was loaded in the wrong encoding"* – samma sak som efter Excel/Anteckningar.
3. `CliApp` kördes, menyval 2 visade *"Inga transaktioner att visa"*, och `e` valdes.
4. `transaktioner.csv` var nu **0 byte**.

#### 2. Felsökning – loggarna
Loggarna från körningen visade hela förloppet:

```
SEVERE: Kunde inte läsa transaktioner.csv: Input length = 1      ← vid start
INFO: Sparade 0 transaktioner till transaktioner.csv             ← vid avslut
```

- `Input length = 1` är `MalformedInputException` från `Files.newBufferedReader`: den hittade 1 byte (`ö` i ANSI)
  som inte är giltig UTF-8. Hela inläsningen avbröts – inte bara den raden – eftersom felet kastas av själva
  läsaren och inte av `franCsvRad`.
- Den andra raden är nyckeln: **`INFO`** – "allt gick bra" – precis när datan förstördes.

**Orsak:** `lasFranFil()` och `sparaTillFil()` visste inget om varandra. Efter en misslyckad läsning var repot
tomt, och `spara()` skriver alltid över hela filen med repots innehåll.

#### 3. Åtgärd
`CliApp` kommer nu ihåg om filen gick att läsa, och vägrar spara över den annars:

```java
private static boolean filenLastesIn = false;

// i lasFranFil(), inne i try – efter att alla transaktioner lagts i repot:
filenLastesIn = true;

// först i sparaTillFil():
if (!filenLastesIn) {
    IO.println("Sparar inte: filen kunde inte läsas vid start, så den skrivs inte över.");
    logger.severe("Sparning stoppad: " + FIL + " kunde inte läsas in vid start, men den skrivs inte över.");
    return;
}
```

Om `las()` kastar `IOException` hoppar Java direkt till `catch`, så raden `filenLastesIn = true;` körs aldrig.

**Verifiering:** samma scenario kördes igen, med både menyval 5 och `e`:

| Scenario | Resultat |
|---|---|
| ANSI-fil, menyval 5 och `e` | *"Sparar inte…"* + `SEVERE`. Filen är **59 byte före och efter** – ingen data förlorad. |
| Vanlig UTF-8-fil | Läses in och sparas som vanligt. |
| Ingen fil alls (första start) | Filen skapas, `filenLastesIn` blir `true`, sparning fungerar. |

Alla 34 tester går fortfarande igenom (`mvn test`).

**Lärdom:** att fånga ett undantag räcker inte – man måste också fråga sig *vad programmet gör efteråt*.
Här gjorde `catch` att programmet överlevde, men det fortsatte med ett felaktigt tillstånd som senare förstörde datan.

### Bugg 5: å, ä och ö från IntelliJ-konsolen sparades som `Ã¶`

**Symptom:** När kategorin `Lön` skrevs in i IntelliJ:s körfönster sparades den i filen som `LÃ¶n`.
Kategorier utan å/ä/ö (t.ex. `Kattmat`) fungerade, så felet syntes inte förrän en kategori med `ö` lades till.

#### 1. Upptäckt – innehållet i filen
Filen var giltig UTF-8 men innehöll fel tecken. Bytes i filen (`od -c`):

```
L 303 203 302 266 n      ← "LÃ¶n": ö har kodats två gånger (4 byte)
L 303 266 n              ← "Lön": så ska det se ut (ö = 2 byte i UTF-8)
```

#### 2. Felsökning – systemegenskaperna
`java -XshowSettings:properties -version` visade hur Java läser och skriver text på datorn:

```
file.encoding   = UTF-8
native.encoding = Cp1252
stdin.encoding  = cp850       ← i ett terminalfönster
```

`stdin.encoding` är alltså **inte** UTF-8. I IntelliJ:s körfönster (som inte är en riktig konsol) blir den
Windows standardtabell Cp1252 – det stämmer med resultatet, eftersom just Cp1252 gör `C3` till `Ã` och `B6` till `¶`.

**Orsak:**
1. IntelliJ:s konsol skickar inmatningen som **UTF-8**, där `ö` är två byte (`C3 B6`).
2. Java läser tangentbordet med **`stdin.encoding`**, som på Windows är en gammal teckentabell (Cp1252). Varje byte
   tolkas då som ett eget tecken: `C3` → `Ã`, `B6` → `¶`.
3. Strängen `LÃ¶n` sparas sedan korrekt som UTF-8 – felet hamnar permanent i filen.

`IO.readln` använder Windows egen konsol när programmet körs i ett riktigt terminalfönster. IntelliJ:s körfönster
är ingen sådan konsol, därför syntes felet där.

#### 3. Åtgärd
Felet ligger i miljön, inte i koden, så det åtgärdades med en JVM-flagga i körkonfigurationen:

```
-Dstdin.encoding=UTF-8
```

Körkonfigurationen sparades som projektfil (*Store as project file*) i `.run/CliApp.run.xml`, så att den följer med
repot. Den som öppnar projektet i IntelliJ – t.ex. läraren – får inställningen automatiskt.

**Verifiering:** samma inmatning (`Lön`) kördes med och utan flaggan:

| Körning | Sparas i filen |
|---|---|
| utan flagga | `LÃ¶n` ❌ |
| med `-Dstdin.encoding=UTF-8` | `Lön` ✅ |

**Lärdom:** en bugg kan sitta i miljön och inte i koden. Det som fungerar på en dator kan bli fel på en annan, så
inställningar som behövs för att köra programmet ska följa med projektet – inte bara finnas lokalt.

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
