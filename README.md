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
│   ├── CliApp.java          # Meny och användarinteraktion
│   ├── Repository.java      # Generisk lagringsklass Repository<T>
│   ├── Transaktion.java     # record: datum, kategori, belopp, typ
│   └── TransaktionTyp.java  # enum: INKOMST, UTGIFT
└── test/java/
    └── RepositoryTest.java  # JUnit 5-tester för Repository<T>
```

## Lösningens uppbyggnad

- **`TransaktionTyp`** – enum med `INKOMST` och `UTGIFT`.
- **`Transaktion`** – ett `record` som håller datum, kategori, belopp och typ. Records är oföränderliga, vilket passar en transaktion som inte ska ändras efter att den skapats.
- **`Repository<T>`** – en egen generisk klass som lagrar objekt i en `ArrayList<T>`:
  - `add(T item)` – lägger till ett objekt.
  - `findAll()` – returnerar en *kopia* av listan så att repot inte kan ändras utifrån.
  - `findWhere(Predicate<T> villkor)` – filtrerar med Stream API och ett lambda-villkor.
- **`CliApp`** – menyn. Validerar indata (typ, belopp > 0, kategori får inte vara tom) innan en transaktion skapas och läggs i repot.

## Status

### Domänmodell
- [x] `enum TransaktionTyp`
- [x] `record Transaktion`
- [x] Generisk klass `Repository<T>` med `add`, `findAll`, `findWhere`
- [ ] Egna undantagsklasser (t.ex. `InvalidTransactionException`, `FileFormatException`)

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
- [ ] Testklass för sammanställnings-/beräkningslogiken
- [ ] Tester för egna undantag med `assertThrows`
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

*Kommer att fyllas i.*

## Visualisering från Plan mode i Claude

Översikt över uppgiften: arkitektur, meny, G/VG-krav som checklista och förslag på arbetsordning.

[Öppna visualiseringen](https://claude.ai/artifact/Fh8swzWQhvkKQG8c3UYsqf)
