# Winter Olympics Manager — Implementation Plan

> **For agentic workers:** This plan is self-contained. You have **zero prior context** for this codebase — everything you need is here. Implement task-by-task, top to bottom. Steps use checkbox (`- [ ]`) syntax for tracking. Follow TDD: write the failing test, see it fail, implement, see it pass, commit. Do not skip ahead; later tasks depend on types defined in earlier ones.

**Goal:** Build a Java console application that manages a Winter Olympics with two competition types (Ski Slalom and Biathlon), computing rankings, medals, statistics, with file output, serialization, exception handling, and a full JUnit 5 test suite.

**Architecture:** Layered design — immutable/POJO **data models** (`model`), **business logic** for ranking and statistics (`competition`, `olympics`), custom **exceptions** (`exception`), an **I/O layer** for text export and Java serialization (`io`), and a **console UI** (`Main` + a menu driver) that supports both interactive `Scanner` input and a hardcoded demo mode. Tests cover models and logic with hardcoded data for high coverage.

**Tech Stack:** Java 21, Gradle 9.5.1 (wrapper present), JUnit 5 (Jupiter, BOM 5.11.4), GitHub Actions CI + Codecov via JaCoCo.

---

## Context You Need (read this first)

**Working directory root** contains a scaffolded Gradle project:

- `build.gradle.kts` — Gradle build, `application` plugin, JDK 21 toolchain, JUnit 5 wired. You will MODIFY this to add JaCoCo.
- `settings.gradle.kts` — `rootProject.name = "winter-olympics"`. Do not change.
- `src/main/java/org/nbu/citb408/olympics/Main.java` — current stub:
  ```java
  package org.nbu.citb408.olympics;

  public class Main {
      public static void main(String[] args) {
          System.out.println("Winter Olympics Manager");
      }
  }
  ```
- Base package: **`org.nbu.citb408.olympics`**. All production code lives under `src/main/java/org/nbu/citb408/olympics/...`; all tests under `src/test/java/org/nbu/citb408/olympics/...`.

**Build & test commands** (use the wrapper, never a system `gradle`):

- Compile + run: `./gradlew run`
- Tests: `./gradlew test`
- Full build (compile, test, jar): `./gradlew build`
- Coverage report: `./gradlew test jacocoTestReport`

**Conventions:**
- English identifiers and English comments/Javadoc.
- Times are in **seconds**, stored as `double`, displayed to **3 decimal places**.
- A "DNF" (Did Not Finish) athlete is excluded from final rankings.
- DRY, YAGNI. Keep each class focused. Commit after each task.

**The assignment requirements (translated), authoritative source of truth:**

1. **Athletes** have: id, name, country, gender, date of birth.
2. **Competition rules:** competitions are run separately for men and women; there is a minimum age to compete; times are measured in seconds to 3 decimal places.
3. **Ski Slalom:** every athlete skis run 1; after run 1 the top finishers (e.g. top 30 by time) advance to run 2; only qualified athletes ski run 2; final ranking is by **sum of both run times**; winner has the smallest total; an athlete who does not finish (DNF) a run is excluded from the final ranking.
4. **Biathlon:** each athlete skis a number of laps; after certain laps there is shooting; each miss adds a penalty (e.g. +1 minute per miss); final time = base time + penalties; ranking by best final time; DNF excluded.
5. **Olympics:** stores the competitions held; produces final rankings per competition; determines medalists (1st/2nd/3rd); counts medals per country; computes average participant age; finds the youngest medalist; writes the final rankings of all competitions to a **text file**; **serializes and deserializes Biathlon data**.

---

## File Structure

Production code (under `src/main/java/org/nbu/citb408/olympics/`):

| File | Responsibility |
|------|----------------|
| `model/Gender.java` | Enum: `MALE`, `FEMALE`. |
| `model/Athlete.java` | Immutable athlete data (id, name, country, gender, dob); `age(asOf)` helper. Serializable. |
| `exception/CompetitionException.java` | Base checked exception for domain rule violations. |
| `exception/IneligibleAthleteException.java` | Thrown when an athlete violates gender/age eligibility. |
| `exception/DuplicateRegistrationException.java` | Thrown when the same athlete is registered twice. |
| `model/Result.java` | Marker/base contract: `athlete()`, `finalTime()`, `isDnf()`. |
| `competition/Competition.java` | Abstract base: name, gender, minAge, registration with eligibility checks, ranking contract. |
| `competition/SkiSlalomResult.java` | Run1/run2 times, DNF flag, combined time. |
| `competition/SkiSlalom.java` | Two-run logic, qualification cutoff, combined-time ranking. |
| `competition/BiathlonResult.java` | Base time, misses, penalty; final time. Serializable. |
| `competition/Biathlon.java` | Penalty computation, final-time ranking. Serializable. |
| `olympics/Medal.java` | Enum: `GOLD`, `SILVER`, `BRONZE`. |
| `olympics/Medalist.java` | Record linking athlete + medal + competition name. |
| `olympics/Olympics.java` | Aggregates competitions; medal table; medals per country; avg age; youngest medalist. |
| `io/RankingWriter.java` | Writes all final rankings to a text file. |
| `io/BiathlonSerializer.java` | Serializes/deserializes `Biathlon` objects. |
| `ConsoleApp.java` | Interactive `Scanner`-driven menu. |
| `DemoData.java` | Builds a sample Olympics (used by demo mode and reused in tests). |
| `Main.java` | Entry point: `--demo` runs demo, otherwise interactive menu. |

Test code (under `src/test/java/org/nbu/citb408/olympics/`): one test class per logic-bearing class (see tasks).

---

## Task 0: Add JaCoCo coverage + CI scaffolding

**Files:**
- Modify: `build.gradle.kts`
- Create: `.github/workflows/ci.yml`

- [ ] **Step 1: Add the JaCoCo plugin and report wiring to `build.gradle.kts`**

Replace the entire contents of `build.gradle.kts` with:

```kotlin
plugins {
    application
    jacoco
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass = "org.nbu.citb408.olympics.Main"
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}
```

- [ ] **Step 2: Verify the build still works with no sources yet**

Run: `./gradlew build`
Expected: `BUILD SUCCESSFUL` (only the `Main` stub compiles; no tests yet).

- [ ] **Step 3: Create the CI workflow**

Create `.github/workflows/ci.yml`:

```yaml
name: CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'

      - name: Set up Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Build and test
        run: ./gradlew build jacocoTestReport

      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v4
        with:
          files: build/reports/jacoco/test/jacocoTestReport.xml
          fail_ci_if_error: false
        env:
          CODECOV_TOKEN: ${{ secrets.CODECOV_TOKEN }}
```

- [ ] **Step 4: Commit**

```bash
git add build.gradle.kts .github/workflows/ci.yml
git commit -m "chore: add jacoco coverage and github actions ci"
```

---

## Task 1: `Gender` enum

**Files:**
- Create: `src/main/java/org/nbu/citb408/olympics/model/Gender.java`

- [ ] **Step 1: Create the enum**

```java
package org.nbu.citb408.olympics.model;

/** Gender categories for competition separation. */
public enum Gender {
    MALE,
    FEMALE
}
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew compileJava`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/nbu/citb408/olympics/model/Gender.java
git commit -m "feat: add Gender enum"
```

---

## Task 2: `Athlete` model (with TDD)

**Files:**
- Create: `src/main/java/org/nbu/citb408/olympics/model/Athlete.java`
- Test: `src/test/java/org/nbu/citb408/olympics/model/AthleteTest.java`

`Athlete` is immutable, `Serializable`, with `equals`/`hashCode` by `id`, and an `age(LocalDate asOf)` helper.

- [ ] **Step 1: Write the failing test**

```java
package org.nbu.citb408.olympics.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class AthleteTest {

    @Test
    void computesAgeAsOfGivenDate() {
        Athlete a = new Athlete(1, "Marco Rossi", "ITA", Gender.MALE,
                LocalDate.of(2000, 6, 15));
        assertEquals(25, a.age(LocalDate.of(2025, 6, 15)));
        assertEquals(24, a.age(LocalDate.of(2025, 6, 14)));
    }

    @Test
    void equalsAndHashCodeByIdOnly() {
        Athlete a = new Athlete(7, "A", "BUL", Gender.FEMALE, LocalDate.of(1999, 1, 1));
        Athlete b = new Athlete(7, "Different", "GER", Gender.MALE, LocalDate.of(1990, 2, 2));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void rejectsNullName() {
        assertThrows(NullPointerException.class,
                () -> new Athlete(1, null, "ITA", Gender.MALE, LocalDate.of(2000, 1, 1)));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests "org.nbu.citb408.olympics.model.AthleteTest"`
Expected: FAIL with compilation error (`Athlete` does not exist).

- [ ] **Step 3: Implement `Athlete`**

```java
package org.nbu.citb408.olympics.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

/** Immutable athlete record. Equality is by {@code id}. */
public final class Athlete implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int id;
    private final String name;
    private final String country;
    private final Gender gender;
    private final LocalDate dateOfBirth;

    public Athlete(int id, String name, String country, Gender gender, LocalDate dateOfBirth) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "name");
        this.country = Objects.requireNonNull(country, "country");
        this.gender = Objects.requireNonNull(gender, "gender");
        this.dateOfBirth = Objects.requireNonNull(dateOfBirth, "dateOfBirth");
    }

    public int id() { return id; }
    public String name() { return name; }
    public String country() { return country; }
    public Gender gender() { return gender; }
    public LocalDate dateOfBirth() { return dateOfBirth; }

    /** Age in whole years as of {@code asOf}. */
    public int age(LocalDate asOf) {
        return Period.between(dateOfBirth, asOf).getYears();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Athlete other)) return false;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "#" + id + " " + name + " (" + country + ")";
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew test --tests "org.nbu.citb408.olympics.model.AthleteTest"`
Expected: PASS (3 tests).

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/nbu/citb408/olympics/model/Athlete.java src/test/java/org/nbu/citb408/olympics/model/AthleteTest.java
git commit -m "feat: add Athlete model with age and id-based equality"
```

---

## Task 3: Domain exceptions

**Files:**
- Create: `src/main/java/org/nbu/citb408/olympics/exception/CompetitionException.java`
- Create: `src/main/java/org/nbu/citb408/olympics/exception/IneligibleAthleteException.java`
- Create: `src/main/java/org/nbu/citb408/olympics/exception/DuplicateRegistrationException.java`

- [ ] **Step 1: Create the base exception**

```java
package org.nbu.citb408.olympics.exception;

/** Base checked exception for domain rule violations. */
public class CompetitionException extends Exception {
    public CompetitionException(String message) {
        super(message);
    }
}
```

- [ ] **Step 2: Create `IneligibleAthleteException`**

```java
package org.nbu.citb408.olympics.exception;

/** Thrown when an athlete fails the gender or minimum-age eligibility rules. */
public class IneligibleAthleteException extends CompetitionException {
    public IneligibleAthleteException(String message) {
        super(message);
    }
}
```

- [ ] **Step 3: Create `DuplicateRegistrationException`**

```java
package org.nbu.citb408.olympics.exception;

/** Thrown when an athlete is registered into the same competition twice. */
public class DuplicateRegistrationException extends CompetitionException {
    public DuplicateRegistrationException(String message) {
        super(message);
    }
}
```

- [ ] **Step 4: Verify compilation**

Run: `./gradlew compileJava`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/nbu/citb408/olympics/exception/
git commit -m "feat: add domain exception hierarchy"
```

---

## Task 4: `Result` contract + abstract `Competition` (with TDD on eligibility)

**Files:**
- Create: `src/main/java/org/nbu/citb408/olympics/model/Result.java`
- Create: `src/main/java/org/nbu/citb408/olympics/competition/Competition.java`
- Test: `src/test/java/org/nbu/citb408/olympics/competition/CompetitionEligibilityTest.java`

`Competition` is abstract: holds name, gender category, minimum age, a reference date used for age checks, and the registered athletes. `register(...)` enforces gender match, minimum age, and rejects duplicates. Subclasses implement `finalRanking()`.

- [ ] **Step 1: Create the `Result` contract**

```java
package org.nbu.citb408.olympics.model;

/** A single athlete's outcome within a competition. */
public interface Result {
    Athlete athlete();

    /** Final classified time in seconds (lower is better). */
    double finalTime();

    /** True if the athlete did not finish and is excluded from ranking. */
    boolean isDnf();
}
```

- [ ] **Step 2: Write the failing eligibility test**

```java
package org.nbu.citb408.olympics.competition;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.nbu.citb408.olympics.exception.DuplicateRegistrationException;
import org.nbu.citb408.olympics.exception.IneligibleAthleteException;
import org.nbu.citb408.olympics.model.Athlete;
import org.nbu.citb408.olympics.model.Gender;
import org.nbu.citb408.olympics.model.Result;

class CompetitionEligibilityTest {

    /** Minimal concrete subclass for testing the abstract base. */
    static final class Dummy extends Competition {
        Dummy() {
            super("Dummy", Gender.MALE, 18, LocalDate.of(2026, 2, 1));
        }
        @Override
        public List<Result> finalRanking() {
            return List.of();
        }
    }

    private Athlete male(int id, int birthYear) {
        return new Athlete(id, "A" + id, "BUL", Gender.MALE, LocalDate.of(birthYear, 1, 1));
    }

    @Test
    void rejectsWrongGender() {
        Dummy c = new Dummy();
        Athlete female = new Athlete(1, "F", "BUL", Gender.FEMALE, LocalDate.of(2000, 1, 1));
        assertThrows(IneligibleAthleteException.class, () -> c.register(female));
    }

    @Test
    void rejectsUnderageAthlete() {
        Dummy c = new Dummy();
        // Born 2010 -> age 16 as of 2026-02-01, below minimum 18.
        assertThrows(IneligibleAthleteException.class, () -> c.register(male(2, 2010)));
    }

    @Test
    void acceptsEligibleAthlete() throws Exception {
        Dummy c = new Dummy();
        c.register(male(3, 2000));
        assertEquals(1, c.athletes().size());
    }

    @Test
    void rejectsDuplicateRegistration() throws Exception {
        Dummy c = new Dummy();
        c.register(male(4, 2000));
        assertThrows(DuplicateRegistrationException.class, () -> c.register(male(4, 2000)));
    }
}
```

- [ ] **Step 3: Run test to verify it fails**

Run: `./gradlew test --tests "org.nbu.citb408.olympics.competition.CompetitionEligibilityTest"`
Expected: FAIL with compilation error (`Competition` does not exist).

- [ ] **Step 4: Implement abstract `Competition`**

```java
package org.nbu.citb408.olympics.competition;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.nbu.citb408.olympics.exception.DuplicateRegistrationException;
import org.nbu.citb408.olympics.exception.IneligibleAthleteException;
import org.nbu.citb408.olympics.model.Athlete;
import org.nbu.citb408.olympics.model.Gender;
import org.nbu.citb408.olympics.model.Result;

/** Base class for a single competition run for one gender category. */
public abstract class Competition implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final Gender genderCategory;
    private final int minAge;
    private final LocalDate referenceDate;
    private final List<Athlete> athletes = new ArrayList<>();

    protected Competition(String name, Gender genderCategory, int minAge, LocalDate referenceDate) {
        this.name = Objects.requireNonNull(name, "name");
        this.genderCategory = Objects.requireNonNull(genderCategory, "genderCategory");
        if (minAge < 0) {
            throw new IllegalArgumentException("minAge must be >= 0");
        }
        this.minAge = minAge;
        this.referenceDate = Objects.requireNonNull(referenceDate, "referenceDate");
    }

    public String name() { return name; }
    public Gender genderCategory() { return genderCategory; }
    public int minAge() { return minAge; }
    public LocalDate referenceDate() { return referenceDate; }

    /** Unmodifiable view of registered athletes. */
    public List<Athlete> athletes() {
        return Collections.unmodifiableList(athletes);
    }

    /**
     * Registers an athlete, enforcing gender, minimum age, and uniqueness.
     *
     * @throws IneligibleAthleteException if gender mismatches or athlete is underage
     * @throws DuplicateRegistrationException if already registered
     */
    public void register(Athlete athlete)
            throws IneligibleAthleteException, DuplicateRegistrationException {
        Objects.requireNonNull(athlete, "athlete");
        if (athlete.gender() != genderCategory) {
            throw new IneligibleAthleteException(
                    athlete + " gender " + athlete.gender()
                            + " does not match category " + genderCategory);
        }
        if (athlete.age(referenceDate) < minAge) {
            throw new IneligibleAthleteException(
                    athlete + " is below minimum age " + minAge);
        }
        if (athletes.contains(athlete)) {
            throw new DuplicateRegistrationException(athlete + " is already registered");
        }
        athletes.add(athlete);
    }

    /** Final classification: finishers only, best (lowest) time first. */
    public abstract List<Result> finalRanking();
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew test --tests "org.nbu.citb408.olympics.competition.CompetitionEligibilityTest"`
Expected: PASS (4 tests).

- [ ] **Step 6: Commit**

```bash
git add src/main/java/org/nbu/citb408/olympics/model/Result.java src/main/java/org/nbu/citb408/olympics/competition/Competition.java src/test/java/org/nbu/citb408/olympics/competition/CompetitionEligibilityTest.java
git commit -m "feat: add Result contract and abstract Competition with eligibility rules"
```

---

## Task 5: Ski Slalom (with TDD)

**Files:**
- Create: `src/main/java/org/nbu/citb408/olympics/competition/SkiSlalomResult.java`
- Create: `src/main/java/org/nbu/citb408/olympics/competition/SkiSlalom.java`
- Test: `src/test/java/org/nbu/citb408/olympics/competition/SkiSlalomTest.java`

**Rules implemented:** record run-1 times; `qualifiers(n)` returns the top `n` finishers of run 1 by time (DNF excluded); record run-2 times; `finalRanking()` returns only athletes who finished **both** runs, ordered by combined time ascending.

- [ ] **Step 1: Create `SkiSlalomResult`**

```java
package org.nbu.citb408.olympics.competition;

import org.nbu.citb408.olympics.model.Athlete;
import org.nbu.citb408.olympics.model.Result;

/** Ski slalom outcome: two run times. A run time of {@code -1} marks a DNF in that run. */
public final class SkiSlalomResult implements Result {

    public static final double DNF = -1.0;

    private final Athlete athlete;
    private double run1 = Double.NaN;
    private double run2 = Double.NaN;

    public SkiSlalomResult(Athlete athlete) {
        this.athlete = athlete;
    }

    @Override
    public Athlete athlete() { return athlete; }

    public void setRun1(double seconds) { this.run1 = seconds; }
    public void setRun2(double seconds) { this.run2 = seconds; }

    public double run1() { return run1; }
    public double run2() { return run2; }

    public boolean run1Recorded() { return !Double.isNaN(run1); }
    public boolean run1Finished() { return run1Recorded() && run1 != DNF; }
    public boolean run2Finished() { return !Double.isNaN(run2) && run2 != DNF; }

    @Override
    public boolean isDnf() {
        return !run1Finished() || !run2Finished();
    }

    /** Combined time of both runs; only meaningful when {@code !isDnf()}. */
    @Override
    public double finalTime() {
        return run1 + run2;
    }
}
```

- [ ] **Step 2: Write the failing test**

```java
package org.nbu.citb408.olympics.competition;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.nbu.citb408.olympics.model.Athlete;
import org.nbu.citb408.olympics.model.Gender;
import org.nbu.citb408.olympics.model.Result;

class SkiSlalomTest {

    private Athlete man(int id) {
        return new Athlete(id, "M" + id, "BUL", Gender.MALE, LocalDate.of(1995, 1, 1));
    }

    private SkiSlalom newSlalom() {
        return new SkiSlalom("Men's Slalom", Gender.MALE, 16, LocalDate.of(2026, 2, 1));
    }

    @Test
    void qualifiersAreTopNByRun1ExcludingDnf() throws Exception {
        SkiSlalom s = newSlalom();
        s.register(man(1));
        s.register(man(2));
        s.register(man(3));
        s.recordRun1(man(1), 50.0);
        s.recordRun1(man(2), 48.0);
        s.recordRun1(man(3), SkiSlalomResult.DNF);

        List<Athlete> top2 = s.qualifiers(2);
        assertEquals(2, top2.size());
        assertEquals(man(2), top2.get(0)); // fastest first
        assertEquals(man(1), top2.get(1));
    }

    @Test
    void finalRankingByCombinedTimeExcludesUnfinished() throws Exception {
        SkiSlalom s = newSlalom();
        s.register(man(1));
        s.register(man(2));
        s.recordRun1(man(1), 50.0);
        s.recordRun1(man(2), 48.0);
        s.recordRun2(man(1), 49.0); // total 99.0
        s.recordRun2(man(2), 52.5); // total 100.5

        List<Result> ranking = s.finalRanking();
        assertEquals(2, ranking.size());
        assertEquals(man(1), ranking.get(0).athlete()); // 99.0 wins
        assertEquals(99.0, ranking.get(0).finalTime(), 1e-9);
    }

    @Test
    void dnfInRun2ExcludedFromFinalRanking() throws Exception {
        SkiSlalom s = newSlalom();
        s.register(man(1));
        s.register(man(2));
        s.recordRun1(man(1), 50.0);
        s.recordRun1(man(2), 48.0);
        s.recordRun2(man(1), 49.0);
        s.recordRun2(man(2), SkiSlalomResult.DNF);

        List<Result> ranking = s.finalRanking();
        assertEquals(1, ranking.size());
        assertEquals(man(1), ranking.get(0).athlete());
    }

    @Test
    void recordingTimeForUnregisteredAthleteThrows() {
        SkiSlalom s = newSlalom();
        assertThrows(IllegalArgumentException.class, () -> s.recordRun1(man(9), 50.0));
    }
}
```

- [ ] **Step 3: Run test to verify it fails**

Run: `./gradlew test --tests "org.nbu.citb408.olympics.competition.SkiSlalomTest"`
Expected: FAIL with compilation error (`SkiSlalom` does not exist).

- [ ] **Step 4: Implement `SkiSlalom`**

```java
package org.nbu.citb408.olympics.competition;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.nbu.citb408.olympics.model.Athlete;
import org.nbu.citb408.olympics.model.Gender;
import org.nbu.citb408.olympics.model.Result;

/** Two-run ski slalom. Final ranking is by the sum of both run times. */
public final class SkiSlalom extends Competition {

    private static final long serialVersionUID = 1L;

    private final Map<Athlete, SkiSlalomResult> results = new LinkedHashMap<>();

    public SkiSlalom(String name, Gender genderCategory, int minAge, LocalDate referenceDate) {
        super(name, genderCategory, minAge, referenceDate);
    }

    private SkiSlalomResult resultFor(Athlete athlete) {
        if (!athletes().contains(athlete)) {
            throw new IllegalArgumentException(athlete + " is not registered in " + name());
        }
        return results.computeIfAbsent(athlete, SkiSlalomResult::new);
    }

    public void recordRun1(Athlete athlete, double seconds) {
        resultFor(athlete).setRun1(seconds);
    }

    public void recordRun2(Athlete athlete, double seconds) {
        resultFor(athlete).setRun2(seconds);
    }

    /** Top {@code n} run-1 finishers, fastest first (DNF excluded). */
    public List<Athlete> qualifiers(int n) {
        return results.values().stream()
                .filter(SkiSlalomResult::run1Finished)
                .sorted(Comparator.comparingDouble(SkiSlalomResult::run1))
                .limit(n)
                .map(SkiSlalomResult::athlete)
                .toList();
    }

    @Override
    public List<Result> finalRanking() {
        List<Result> ranked = new ArrayList<>(results.values().stream()
                .filter(r -> !r.isDnf())
                .sorted(Comparator.comparingDouble(SkiSlalomResult::finalTime))
                .map(r -> (Result) r)
                .toList());
        return ranked;
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew test --tests "org.nbu.citb408.olympics.competition.SkiSlalomTest"`
Expected: PASS (4 tests).

- [ ] **Step 6: Commit**

```bash
git add src/main/java/org/nbu/citb408/olympics/competition/SkiSlalomResult.java src/main/java/org/nbu/citb408/olympics/competition/SkiSlalom.java src/test/java/org/nbu/citb408/olympics/competition/SkiSlalomTest.java
git commit -m "feat: add ski slalom with two-run qualification and combined-time ranking"
```

---

## Task 6: Biathlon (with TDD)

**Files:**
- Create: `src/main/java/org/nbu/citb408/olympics/competition/BiathlonResult.java`
- Create: `src/main/java/org/nbu/citb408/olympics/competition/Biathlon.java`
- Test: `src/test/java/org/nbu/citb408/olympics/competition/BiathlonTest.java`

**Rules implemented:** record base ski time + number of missed shots; final time = base + (misses × penaltyPerMissSeconds). Default penalty is **60 seconds** (1 minute) per miss. DNF excluded; ranking by final time ascending. `Biathlon` and `BiathlonResult` are `Serializable` (assignment requires serialization of biathlon data).

- [ ] **Step 1: Create `BiathlonResult`**

```java
package org.nbu.citb408.olympics.competition;

import java.io.Serializable;
import org.nbu.citb408.olympics.model.Athlete;
import org.nbu.citb408.olympics.model.Result;

/** Biathlon outcome: base ski time plus shooting penalties. */
public final class BiathlonResult implements Result, Serializable {

    private static final long serialVersionUID = 1L;

    private final Athlete athlete;
    private final double penaltyPerMissSeconds;
    private double baseTime = Double.NaN;
    private int misses = 0;
    private boolean dnf = false;

    public BiathlonResult(Athlete athlete, double penaltyPerMissSeconds) {
        this.athlete = athlete;
        this.penaltyPerMissSeconds = penaltyPerMissSeconds;
    }

    @Override
    public Athlete athlete() { return athlete; }

    public void setBaseTime(double seconds) { this.baseTime = seconds; }
    public void setMisses(int misses) {
        if (misses < 0) {
            throw new IllegalArgumentException("misses must be >= 0");
        }
        this.misses = misses;
    }
    public void markDnf() { this.dnf = true; }

    public double baseTime() { return baseTime; }
    public int misses() { return misses; }
    public double penaltySeconds() { return misses * penaltyPerMissSeconds; }

    @Override
    public boolean isDnf() {
        return dnf || Double.isNaN(baseTime);
    }

    @Override
    public double finalTime() {
        return baseTime + penaltySeconds();
    }
}
```

- [ ] **Step 2: Write the failing test**

```java
package org.nbu.citb408.olympics.competition;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.nbu.citb408.olympics.model.Athlete;
import org.nbu.citb408.olympics.model.Gender;
import org.nbu.citb408.olympics.model.Result;

class BiathlonTest {

    private Athlete woman(int id) {
        return new Athlete(id, "W" + id, "GER", Gender.FEMALE, LocalDate.of(1996, 1, 1));
    }

    private Biathlon newBiathlon() {
        // default 60s per miss
        return new Biathlon("Women's Biathlon", Gender.FEMALE, 18, LocalDate.of(2026, 2, 1));
    }

    @Test
    void finalTimeAddsPenaltyPerMiss() throws Exception {
        Biathlon b = newBiathlon();
        b.register(woman(1));
        b.recordResult(woman(1), 1200.0, 2); // 1200 + 2*60 = 1320
        List<Result> ranking = b.finalRanking();
        assertEquals(1, ranking.size());
        assertEquals(1320.0, ranking.get(0).finalTime(), 1e-9);
    }

    @Test
    void rankingByFinalTimeAscending() throws Exception {
        Biathlon b = newBiathlon();
        b.register(woman(1));
        b.register(woman(2));
        b.recordResult(woman(1), 1200.0, 3); // 1380
        b.recordResult(woman(2), 1300.0, 0); // 1300 -> wins
        List<Result> ranking = b.finalRanking();
        assertEquals(woman(2), ranking.get(0).athlete());
        assertEquals(woman(1), ranking.get(1).athlete());
    }

    @Test
    void dnfExcludedFromRanking() throws Exception {
        Biathlon b = newBiathlon();
        b.register(woman(1));
        b.register(woman(2));
        b.recordResult(woman(1), 1200.0, 1);
        b.markDnf(woman(2));
        List<Result> ranking = b.finalRanking();
        assertEquals(1, ranking.size());
        assertEquals(woman(1), ranking.get(0).athlete());
    }

    @Test
    void customPenaltyPerMiss() throws Exception {
        Biathlon b = new Biathlon("Custom", Gender.FEMALE, 18, LocalDate.of(2026, 2, 1), 30.0);
        b.register(woman(1));
        b.recordResult(woman(1), 1000.0, 4); // 1000 + 4*30 = 1120
        assertEquals(1120.0, b.finalRanking().get(0).finalTime(), 1e-9);
    }
}
```

- [ ] **Step 3: Run test to verify it fails**

Run: `./gradlew test --tests "org.nbu.citb408.olympics.competition.BiathlonTest"`
Expected: FAIL with compilation error (`Biathlon` does not exist).

- [ ] **Step 4: Implement `Biathlon`**

```java
package org.nbu.citb408.olympics.competition;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.nbu.citb408.olympics.model.Athlete;
import org.nbu.citb408.olympics.model.Gender;
import org.nbu.citb408.olympics.model.Result;

/** Biathlon: base ski time plus a fixed penalty per missed shot. */
public final class Biathlon extends Competition {

    private static final long serialVersionUID = 1L;

    /** Default penalty: one minute per missed shot. */
    public static final double DEFAULT_PENALTY_SECONDS = 60.0;

    private final double penaltyPerMissSeconds;
    private final Map<Athlete, BiathlonResult> results = new LinkedHashMap<>();

    public Biathlon(String name, Gender genderCategory, int minAge, LocalDate referenceDate) {
        this(name, genderCategory, minAge, referenceDate, DEFAULT_PENALTY_SECONDS);
    }

    public Biathlon(String name, Gender genderCategory, int minAge, LocalDate referenceDate,
                    double penaltyPerMissSeconds) {
        super(name, genderCategory, minAge, referenceDate);
        if (penaltyPerMissSeconds < 0) {
            throw new IllegalArgumentException("penalty must be >= 0");
        }
        this.penaltyPerMissSeconds = penaltyPerMissSeconds;
    }

    public double penaltyPerMissSeconds() { return penaltyPerMissSeconds; }

    private BiathlonResult resultFor(Athlete athlete) {
        if (!athletes().contains(athlete)) {
            throw new IllegalArgumentException(athlete + " is not registered in " + name());
        }
        return results.computeIfAbsent(athlete,
                a -> new BiathlonResult(a, penaltyPerMissSeconds));
    }

    public void recordResult(Athlete athlete, double baseTimeSeconds, int misses) {
        BiathlonResult r = resultFor(athlete);
        r.setBaseTime(baseTimeSeconds);
        r.setMisses(misses);
    }

    public void markDnf(Athlete athlete) {
        resultFor(athlete).markDnf();
    }

    @Override
    public List<Result> finalRanking() {
        return results.values().stream()
                .filter(r -> !r.isDnf())
                .sorted(Comparator.comparingDouble(BiathlonResult::finalTime))
                .map(r -> (Result) r)
                .toList();
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew test --tests "org.nbu.citb408.olympics.competition.BiathlonTest"`
Expected: PASS (4 tests).

- [ ] **Step 6: Commit**

```bash
git add src/main/java/org/nbu/citb408/olympics/competition/BiathlonResult.java src/main/java/org/nbu/citb408/olympics/competition/Biathlon.java src/test/java/org/nbu/citb408/olympics/competition/BiathlonTest.java
git commit -m "feat: add biathlon with penalty-based final time ranking"
```

---

## Task 7: Medal types

**Files:**
- Create: `src/main/java/org/nbu/citb408/olympics/olympics/Medal.java`
- Create: `src/main/java/org/nbu/citb408/olympics/olympics/Medalist.java`

- [ ] **Step 1: Create `Medal`**

```java
package org.nbu.citb408.olympics.olympics;

/** Olympic medal ranks. Ordinal matches placement (GOLD=0,...). */
public enum Medal {
    GOLD,
    SILVER,
    BRONZE
}
```

- [ ] **Step 2: Create `Medalist`**

```java
package org.nbu.citb408.olympics.olympics;

import org.nbu.citb408.olympics.model.Athlete;

/** An athlete who won a medal in a named competition. */
public record Medalist(Athlete athlete, Medal medal, String competitionName) {
}
```

- [ ] **Step 3: Verify compilation**

Run: `./gradlew compileJava`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/org/nbu/citb408/olympics/olympics/Medal.java src/main/java/org/nbu/citb408/olympics/olympics/Medalist.java
git commit -m "feat: add Medal enum and Medalist record"
```

---

## Task 8: `Olympics` aggregate (with TDD)

**Files:**
- Create: `src/main/java/org/nbu/citb408/olympics/olympics/Olympics.java`
- Test: `src/test/java/org/nbu/citb408/olympics/olympics/OlympicsTest.java`

**Responsibilities:** hold competitions; `medalists()` returns top-3 finishers (per competition) as `Medalist` objects; `medalCountByCountry()` maps country → medal count; `averageAge(asOf)` averages participant ages over all distinct registered athletes; `youngestMedalist(asOf)` returns the medalist with the latest date of birth.

- [ ] **Step 1: Write the failing test**

```java
package org.nbu.citb408.olympics.olympics;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.nbu.citb408.olympics.competition.Biathlon;
import org.nbu.citb408.olympics.model.Athlete;
import org.nbu.citb408.olympics.model.Gender;

class OlympicsTest {

    private static final LocalDate REF = LocalDate.of(2026, 2, 1);

    private Athlete woman(int id, String country, int birthYear) {
        return new Athlete(id, "W" + id, country, Gender.FEMALE, LocalDate.of(birthYear, 1, 1));
    }

    private Biathlon biathlonWith(Athlete... as) throws Exception {
        Biathlon b = new Biathlon("Women's Biathlon", Gender.FEMALE, 18, REF);
        double t = 1000.0;
        for (Athlete a : as) {
            b.register(a);
            b.recordResult(a, t, 0);
            t += 10.0; // each subsequent athlete is slower
        }
        return b;
    }

    @Test
    void medalistsAreTopThree() throws Exception {
        Athlete a1 = woman(1, "GER", 1996);
        Athlete a2 = woman(2, "BUL", 1998);
        Athlete a3 = woman(3, "ITA", 2000);
        Athlete a4 = woman(4, "FRA", 1995);
        Olympics o = new Olympics("Winter 2026");
        o.addCompetition(biathlonWith(a1, a2, a3, a4));

        List<Medalist> medalists = o.medalists();
        assertEquals(3, medalists.size());
        assertEquals(Medal.GOLD, medalists.get(0).medal());
        assertEquals(a1, medalists.get(0).athlete());
        assertEquals(Medal.SILVER, medalists.get(1).medal());
        assertEquals(a2, medalists.get(1).athlete());
        assertEquals(Medal.BRONZE, medalists.get(2).medal());
        assertEquals(a3, medalists.get(2).athlete());
    }

    @Test
    void medalCountByCountry() throws Exception {
        Olympics o = new Olympics("Winter 2026");
        o.addCompetition(biathlonWith(
                woman(1, "GER", 1996), woman(2, "GER", 1998), woman(3, "ITA", 2000)));
        Map<String, Long> counts = o.medalCountByCountry();
        assertEquals(2L, counts.get("GER"));
        assertEquals(1L, counts.get("ITA"));
    }

    @Test
    void averageAgeOverDistinctParticipants() throws Exception {
        Olympics o = new Olympics("Winter 2026");
        // born 1996 -> 30, born 2006 -> 20 ; average 25
        o.addCompetition(biathlonWith(woman(1, "GER", 1996), woman(2, "BUL", 2006)));
        assertEquals(25.0, o.averageAge(REF), 1e-9);
    }

    @Test
    void youngestMedalistHasLatestBirthDate() throws Exception {
        Athlete older = woman(1, "GER", 1990);
        Athlete youngest = woman(2, "BUL", 2004);
        Athlete mid = woman(3, "ITA", 1998);
        Olympics o = new Olympics("Winter 2026");
        // order by time: older fastest, youngest second, mid third -> all medal
        o.addCompetition(biathlonWith(older, youngest, mid));
        Medalist y = o.youngestMedalist(REF);
        assertEquals(youngest, y.athlete());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests "org.nbu.citb408.olympics.olympics.OlympicsTest"`
Expected: FAIL with compilation error (`Olympics` does not exist).

- [ ] **Step 3: Implement `Olympics`**

```java
package org.nbu.citb408.olympics.olympics;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.nbu.citb408.olympics.competition.Competition;
import org.nbu.citb408.olympics.model.Athlete;
import org.nbu.citb408.olympics.model.Result;

/** Aggregates competitions and computes medals and statistics. */
public final class Olympics {

    private final String name;
    private final List<Competition> competitions = new ArrayList<>();

    public Olympics(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public String name() { return name; }

    public void addCompetition(Competition competition) {
        competitions.add(Objects.requireNonNull(competition, "competition"));
    }

    public List<Competition> competitions() {
        return Collections.unmodifiableList(competitions);
    }

    /** Top-3 finishers of every competition, as medalists. */
    public List<Medalist> medalists() {
        List<Medalist> all = new ArrayList<>();
        Medal[] medals = Medal.values();
        for (Competition c : competitions) {
            List<Result> ranking = c.finalRanking();
            for (int i = 0; i < ranking.size() && i < medals.length; i++) {
                all.add(new Medalist(ranking.get(i).athlete(), medals[i], c.name()));
            }
        }
        return all;
    }

    /** Country -> number of medals won. */
    public Map<String, Long> medalCountByCountry() {
        return medalists().stream()
                .collect(Collectors.groupingBy(
                        m -> m.athlete().country(),
                        LinkedHashMap::new,
                        Collectors.counting()));
    }

    /** Average age (whole years) of all distinct registered athletes. */
    public double averageAge(LocalDate asOf) {
        Set<Athlete> distinct = new HashSet<>();
        for (Competition c : competitions) {
            distinct.addAll(c.athletes());
        }
        return distinct.stream()
                .mapToInt(a -> a.age(asOf))
                .average()
                .orElse(0.0);
    }

    /** The medalist with the latest date of birth, or null if no medals awarded. */
    public Medalist youngestMedalist(LocalDate asOf) {
        return medalists().stream()
                .max(Comparator.comparing(m -> m.athlete().dateOfBirth()))
                .orElse(null);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew test --tests "org.nbu.citb408.olympics.olympics.OlympicsTest"`
Expected: PASS (4 tests).

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/nbu/citb408/olympics/olympics/Olympics.java src/test/java/org/nbu/citb408/olympics/olympics/OlympicsTest.java
git commit -m "feat: add Olympics aggregate with medals and statistics"
```

---

## Task 9: Text ranking export (with TDD)

**Files:**
- Create: `src/main/java/org/nbu/citb408/olympics/io/RankingWriter.java`
- Test: `src/test/java/org/nbu/citb408/olympics/io/RankingWriterTest.java`

Writes the final ranking of every competition in the Olympics to a UTF-8 text file. Times shown to 3 decimals.

- [ ] **Step 1: Write the failing test**

```java
package org.nbu.citb408.olympics.io;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.nbu.citb408.olympics.competition.Biathlon;
import org.nbu.citb408.olympics.model.Athlete;
import org.nbu.citb408.olympics.model.Gender;
import org.nbu.citb408.olympics.olympics.Olympics;

class RankingWriterTest {

    @Test
    void writesRankingsToFile(@TempDir Path dir) throws Exception {
        LocalDate ref = LocalDate.of(2026, 2, 1);
        Biathlon b = new Biathlon("Women's Biathlon", Gender.FEMALE, 18, ref);
        Athlete a1 = new Athlete(1, "Anna", "GER", Gender.FEMALE, LocalDate.of(1996, 1, 1));
        Athlete a2 = new Athlete(2, "Bea", "BUL", Gender.FEMALE, LocalDate.of(1998, 1, 1));
        b.register(a1);
        b.register(a2);
        b.recordResult(a1, 1000.0, 0); // wins
        b.recordResult(a2, 1010.0, 1); // 1070

        Olympics o = new Olympics("Winter 2026");
        o.addCompetition(b);

        Path out = dir.resolve("rankings.txt");
        new RankingWriter().write(o, out);

        String content = Files.readString(out);
        assertTrue(content.contains("Women's Biathlon"), content);
        assertTrue(content.contains("Anna"), content);
        assertTrue(content.contains("1000.000"), content); // 3-decimal formatting
        // Anna (winner) appears before Bea
        assertTrue(content.indexOf("Anna") < content.indexOf("Bea"), content);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests "org.nbu.citb408.olympics.io.RankingWriterTest"`
Expected: FAIL with compilation error (`RankingWriter` does not exist).

- [ ] **Step 3: Implement `RankingWriter`**

```java
package org.nbu.citb408.olympics.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import org.nbu.citb408.olympics.competition.Competition;
import org.nbu.citb408.olympics.model.Result;
import org.nbu.citb408.olympics.olympics.Olympics;

/** Writes the final rankings of all competitions to a text file. */
public final class RankingWriter {

    /**
     * Writes the rankings to {@code target}, creating parent directories if needed.
     *
     * @throws UncheckedIOException if writing fails
     */
    public void write(Olympics olympics, Path target) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(olympics.name()).append(" — Final Rankings ===")
          .append(System.lineSeparator()).append(System.lineSeparator());

        for (Competition c : olympics.competitions()) {
            sb.append(c.name())
              .append(" (").append(c.genderCategory()).append(")")
              .append(System.lineSeparator());
            List<Result> ranking = c.finalRanking();
            if (ranking.isEmpty()) {
                sb.append("  (no classified athletes)").append(System.lineSeparator());
            } else {
                int place = 1;
                for (Result r : ranking) {
                    sb.append(String.format(Locale.ROOT, "  %2d. %-28s %10.3f s%n",
                            place++, r.athlete().name(), r.finalTime()));
                }
            }
            sb.append(System.lineSeparator());
        }

        try {
            if (target.getParent() != null) {
                Files.createDirectories(target.getParent());
            }
            Files.writeString(target, sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write rankings to " + target, e);
        }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew test --tests "org.nbu.citb408.olympics.io.RankingWriterTest"`
Expected: PASS (1 test).

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/nbu/citb408/olympics/io/RankingWriter.java src/test/java/org/nbu/citb408/olympics/io/RankingWriterTest.java
git commit -m "feat: add text ranking export"
```

---

## Task 10: Biathlon serialization (with TDD)

**Files:**
- Create: `src/main/java/org/nbu/citb408/olympics/io/BiathlonSerializer.java`
- Test: `src/test/java/org/nbu/citb408/olympics/io/BiathlonSerializerTest.java`

Round-trips a `Biathlon` via Java object serialization to satisfy the "serialize/deserialize Biathlon data" requirement.

- [ ] **Step 1: Write the failing test**

```java
package org.nbu.citb408.olympics.io;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.nbu.citb408.olympics.competition.Biathlon;
import org.nbu.citb408.olympics.model.Athlete;
import org.nbu.citb408.olympics.model.Gender;
import org.nbu.citb408.olympics.model.Result;

class BiathlonSerializerTest {

    @Test
    void roundTripsBiathlon(@TempDir Path dir) throws Exception {
        LocalDate ref = LocalDate.of(2026, 2, 1);
        Biathlon b = new Biathlon("Men's Biathlon", Gender.MALE, 18, ref);
        Athlete a = new Athlete(1, "Ivan", "BUL", Gender.MALE, LocalDate.of(1995, 5, 5));
        b.register(a);
        b.recordResult(a, 1500.0, 2); // 1500 + 120 = 1620

        Path file = dir.resolve("biathlon.ser");
        BiathlonSerializer serializer = new BiathlonSerializer();
        serializer.save(b, file);
        Biathlon restored = serializer.load(file);

        assertEquals("Men's Biathlon", restored.name());
        List<Result> ranking = restored.finalRanking();
        assertEquals(1, ranking.size());
        assertEquals("Ivan", ranking.get(0).athlete().name());
        assertEquals(1620.0, ranking.get(0).finalTime(), 1e-9);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests "org.nbu.citb408.olympics.io.BiathlonSerializerTest"`
Expected: FAIL with compilation error (`BiathlonSerializer` does not exist).

- [ ] **Step 3: Implement `BiathlonSerializer`**

```java
package org.nbu.citb408.olympics.io;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.nbu.citb408.olympics.competition.Biathlon;

/** Serializes and deserializes {@link Biathlon} objects via Java serialization. */
public final class BiathlonSerializer {

    /** Serializes {@code biathlon} to {@code target}. */
    public void save(Biathlon biathlon, Path target) {
        try {
            if (target.getParent() != null) {
                Files.createDirectories(target.getParent());
            }
            try (ObjectOutputStream out =
                         new ObjectOutputStream(Files.newOutputStream(target))) {
                out.writeObject(biathlon);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to serialize Biathlon to " + target, e);
        }
    }

    /** Deserializes a {@link Biathlon} from {@code source}. */
    public Biathlon load(Path source) {
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(source))) {
            return (Biathlon) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new UncheckedIOException(
                    new IOException("Failed to deserialize Biathlon from " + source, e));
        }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew test --tests "org.nbu.citb408.olympics.io.BiathlonSerializerTest"`
Expected: PASS (1 test).

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/nbu/citb408/olympics/io/BiathlonSerializer.java src/test/java/org/nbu/citb408/olympics/io/BiathlonSerializerTest.java
git commit -m "feat: add biathlon java serialization round-trip"
```

---

## Task 11: Demo data builder

**Files:**
- Create: `src/main/java/org/nbu/citb408/olympics/DemoData.java`

Builds a fully populated sample `Olympics` (used by demo mode and reusable from tests). All checked exceptions are wrapped so callers get a ready-to-use object.

- [ ] **Step 1: Implement `DemoData`**

```java
package org.nbu.citb408.olympics;

import java.time.LocalDate;
import org.nbu.citb408.olympics.competition.Biathlon;
import org.nbu.citb408.olympics.competition.SkiSlalom;
import org.nbu.citb408.olympics.competition.SkiSlalomResult;
import org.nbu.citb408.olympics.exception.CompetitionException;
import org.nbu.citb408.olympics.model.Athlete;
import org.nbu.citb408.olympics.model.Gender;
import org.nbu.citb408.olympics.olympics.Olympics;

/** Builds a sample Olympics with realistic data for the demo and tests. */
public final class DemoData {

    public static final LocalDate REFERENCE_DATE = LocalDate.of(2026, 2, 1);

    private DemoData() {
    }

    /** Builds a populated Olympics. Wraps domain exceptions as IllegalStateException. */
    public static Olympics build() {
        try {
            return buildInternal();
        } catch (CompetitionException e) {
            throw new IllegalStateException("Demo data is invalid", e);
        }
    }

    private static Olympics buildInternal() throws CompetitionException {
        Olympics olympics = new Olympics("Winter Olympics 2026");

        // --- Men's Ski Slalom ---
        SkiSlalom slalom = new SkiSlalom("Men's Ski Slalom", Gender.MALE, 16, REFERENCE_DATE);
        Athlete m1 = new Athlete(1, "Marco Rossi", "ITA", Gender.MALE, LocalDate.of(1998, 3, 12));
        Athlete m2 = new Athlete(2, "Lukas Mayer", "AUT", Gender.MALE, LocalDate.of(2000, 7, 5));
        Athlete m3 = new Athlete(3, "Henrik Berg", "NOR", Gender.MALE, LocalDate.of(2004, 1, 20));
        Athlete m4 = new Athlete(4, "Pierre Blanc", "FRA", Gender.MALE, LocalDate.of(1996, 11, 2));
        slalom.register(m1);
        slalom.register(m2);
        slalom.register(m3);
        slalom.register(m4);
        slalom.recordRun1(m1, 52.310);
        slalom.recordRun1(m2, 51.870);
        slalom.recordRun1(m3, 53.005);
        slalom.recordRun1(m4, SkiSlalomResult.DNF); // does not finish run 1
        slalom.recordRun2(m1, 51.900);
        slalom.recordRun2(m2, 52.450);
        slalom.recordRun2(m3, 52.800);
        olympics.addCompetition(slalom);

        // --- Women's Biathlon ---
        Biathlon biathlon = new Biathlon("Women's Biathlon", Gender.FEMALE, 18, REFERENCE_DATE);
        Athlete w1 = new Athlete(5, "Anna Schmidt", "GER", Gender.FEMALE, LocalDate.of(1995, 5, 9));
        Athlete w2 = new Athlete(6, "Elin Olsen", "SWE", Gender.FEMALE, LocalDate.of(2003, 2, 14));
        Athlete w3 = new Athlete(7, "Maja Novak", "SLO", Gender.FEMALE, LocalDate.of(1999, 8, 30));
        Athlete w4 = new Athlete(8, "Petya Ivanova", "BUL", Gender.FEMALE, LocalDate.of(2001, 4, 1));
        biathlon.register(w1);
        biathlon.register(w2);
        biathlon.register(w3);
        biathlon.register(w4);
        biathlon.recordResult(w1, 1320.500, 1);
        biathlon.recordResult(w2, 1298.250, 3);
        biathlon.recordResult(w3, 1305.750, 0);
        biathlon.markDnf(w4);
        olympics.addCompetition(biathlon);

        return olympics;
    }
}
```

> Note: `SkiSlalomResult.DNF` is the `-1.0` sentinel from Task 5; `m4` is left without a run-2 time so the demo exercises the DNF-exclusion path. `w4` is marked DNF in the biathlon for the same reason.

- [ ] **Step 2: Verify compilation**

Run: `./gradlew compileJava`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/nbu/citb408/olympics/DemoData.java
git commit -m "feat: add demo data builder"
```

---

## Task 12: Console app + `Main` wiring

**Files:**
- Create: `src/main/java/org/nbu/citb408/olympics/ConsoleApp.java`
- Modify: `src/main/java/org/nbu/citb408/olympics/Main.java`

Interactive `Scanner`-driven menu. `Main` runs the demo when invoked with `--demo`, otherwise launches the interactive menu.

- [ ] **Step 1: Implement `ConsoleApp`**

```java
package org.nbu.citb408.olympics;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import org.nbu.citb408.olympics.competition.Competition;
import org.nbu.citb408.olympics.io.RankingWriter;
import org.nbu.citb408.olympics.model.Result;
import org.nbu.citb408.olympics.olympics.Medalist;
import org.nbu.citb408.olympics.olympics.Olympics;

/** Interactive console menu over a populated Olympics. */
public final class ConsoleApp {

    private final Scanner scanner;
    private final Olympics olympics;

    public ConsoleApp(Scanner scanner, Olympics olympics) {
        this.scanner = scanner;
        this.olympics = olympics;
    }

    /** Runs the menu loop until the user chooses to exit. */
    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.hasNextLine() ? scanner.nextLine().trim() : "0";
            switch (choice) {
                case "1" -> showRankings();
                case "2" -> showMedalists();
                case "3" -> showMedalsByCountry();
                case "4" -> showStatistics();
                case "5" -> exportRankings();
                case "0" -> running = false;
                default -> System.out.println("Unknown option: " + choice);
            }
        }
        System.out.println("Goodbye.");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("=== " + olympics.name() + " ===");
        System.out.println("1) Show rankings");
        System.out.println("2) Show medalists");
        System.out.println("3) Show medals by country");
        System.out.println("4) Show statistics");
        System.out.println("5) Export rankings to file");
        System.out.println("0) Exit");
        System.out.print("Choose: ");
    }

    private void showRankings() {
        for (Competition c : olympics.competitions()) {
            System.out.println();
            System.out.println(c.name() + " (" + c.genderCategory() + ")");
            List<Result> ranking = c.finalRanking();
            if (ranking.isEmpty()) {
                System.out.println("  (no classified athletes)");
            }
            int place = 1;
            for (Result r : ranking) {
                System.out.printf(Locale.ROOT, "  %2d. %-28s %10.3f s%n",
                        place++, r.athlete().name(), r.finalTime());
            }
        }
    }

    private void showMedalists() {
        System.out.println();
        for (Medalist m : olympics.medalists()) {
            System.out.printf("  %-6s %-22s %s%n",
                    m.medal(), m.athlete().name(), m.competitionName());
        }
    }

    private void showMedalsByCountry() {
        System.out.println();
        Map<String, Long> counts = olympics.medalCountByCountry();
        counts.forEach((country, n) -> System.out.printf("  %-5s %d%n", country, n));
    }

    private void showStatistics() {
        System.out.println();
        System.out.printf(Locale.ROOT, "  Average participant age: %.1f%n",
                olympics.averageAge(DemoData.REFERENCE_DATE));
        Medalist youngest = olympics.youngestMedalist(DemoData.REFERENCE_DATE);
        if (youngest != null) {
            System.out.println("  Youngest medalist: " + youngest.athlete().name()
                    + " (" + youngest.athlete().country() + ")");
        } else {
            System.out.println("  Youngest medalist: none");
        }
    }

    private void exportRankings() {
        Path out = Path.of("build", "rankings.txt");
        new RankingWriter().write(olympics, out);
        System.out.println("  Rankings written to " + out.toAbsolutePath());
    }
}
```

- [ ] **Step 2: Replace `Main.java`**

```java
package org.nbu.citb408.olympics;

import java.util.Arrays;
import java.util.Scanner;
import org.nbu.citb408.olympics.olympics.Olympics;

/** Entry point. Use {@code --demo} to print a demo report, otherwise run the menu. */
public final class Main {

    public static void main(String[] args) {
        Olympics olympics = DemoData.build();
        boolean demo = Arrays.asList(args).contains("--demo");

        if (demo) {
            runDemo(olympics);
            return;
        }

        try (Scanner scanner = new Scanner(System.in)) {
            new ConsoleApp(scanner, olympics).run();
        }
    }

    private static void runDemo(Olympics olympics) {
        System.out.println("Winter Olympics Manager — demo mode");
        olympics.competitions().forEach(c -> {
            System.out.println();
            System.out.println(c.name() + " (" + c.genderCategory() + ")");
            int place = 1;
            for (var r : c.finalRanking()) {
                System.out.printf(java.util.Locale.ROOT, "  %2d. %-28s %10.3f s%n",
                        place++, r.athlete().name(), r.finalTime());
            }
        });
        System.out.println();
        System.out.println("Medalists:");
        olympics.medalists().forEach(m ->
                System.out.printf("  %-6s %-22s %s%n",
                        m.medal(), m.athlete().name(), m.competitionName()));
    }
}
```

> Note: demo mode prints reports directly (it does not read stdin). Interactive mode uses `ConsoleApp` with a real `Scanner(System.in)`.

- [ ] **Step 3: Verify build and demo run**

Run: `./gradlew run --args="--demo"`
Expected: prints two competition rankings and a medalist list, `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/org/nbu/citb408/olympics/ConsoleApp.java src/main/java/org/nbu/citb408/olympics/Main.java
git commit -m "feat: add interactive console app and demo entry point"
```

---

## Task 13: Full verification + README update

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Run the full build with coverage**

Run: `./gradlew build jacocoTestReport`
Expected: `BUILD SUCCESSFUL`, all tests pass. Coverage report at `build/reports/jacoco/test/jacocoTestReport.xml`.

- [ ] **Step 2: Update the README status section**

In `README.md`, replace the `## Status` section body with:

```markdown
## Status

Implemented: domain models, Ski Slalom (two-run combined ranking), Biathlon
(penalty-based ranking, serializable), Olympics aggregate (medals, medals per
country, average age, youngest medalist), text ranking export, Biathlon
serialization, interactive console menu, demo mode (`./gradlew run --args="--demo"`),
and a JUnit 5 test suite with JaCoCo coverage and GitHub Actions CI.
```

- [ ] **Step 3: Commit**

```bash
git add README.md
git commit -m "docs: update README status to reflect completed implementation"
```

---

## Self-Review (already performed by plan author)

**Spec coverage check** — every assignment requirement maps to a task:
- Athletes (id/name/country/gender/dob) → Task 2.
- Separate men/women + min age + 3-decimal seconds → Task 4 (eligibility), formatting in Tasks 9/12.
- Ski Slalom: run 1, qualification cutoff, run 2, combined ranking, DNF exclusion → Task 5.
- Biathlon: ski time, misses, penalties, final time, ranking, DNF → Task 6.
- Olympics: store competitions, per-competition rankings, medalists, medals per country, average age, youngest medalist → Task 8.
- Final rankings to a text file → Task 9.
- Serialize/deserialize Biathlon → Task 6 (Serializable) + Task 10 (round-trip).
- Architecture: models / business logic / exception handling / tests → packages `model`, `competition`+`olympics`, `exception`, plus tests in every logic task.
- Extras requested by user: interactive input → Task 12; hardcoded demo data for tests → Task 11 + reused in tests; Codecov + CI → Task 0.

**Type consistency check** — method names referenced across tasks are consistent:
`register`, `athletes()`, `finalRanking()`, `recordRun1/recordRun2`, `qualifiers(n)`,
`recordResult`, `markDnf`, `medalists()`, `medalCountByCountry()`, `averageAge(asOf)`,
`youngestMedalist(asOf)`, `RankingWriter.write`, `BiathlonSerializer.save/load`.

**Placeholder scan** — no TBD/TODO; every code step contains complete code.

---

## Dispatch Notes (for the human orchestrator)

These tasks are mostly **sequential** because later code depends on earlier types. If you parallelize across agents, respect these dependency groups:

- **Group A (independent, can run first in parallel):** Task 0 (build/CI), Task 1 (Gender), Task 3 (exceptions), Task 7 (Medal/Medalist).
- **Group B (after A):** Task 2 (Athlete, needs Gender) → Task 4 (Competition, needs Athlete+exceptions+Result).
- **Group C (after Task 4):** Task 5 (SkiSlalom) and Task 6 (Biathlon) can run in parallel.
- **Group D (after C):** Task 8 (Olympics), then Task 9 (RankingWriter) and Task 10 (BiathlonSerializer) in parallel.
- **Group E (after D):** Task 11 (DemoData) → Task 12 (ConsoleApp/Main) → Task 13 (verify).

Each task ends with `./gradlew` verification and a commit, so an agent can confirm success independently. If any test fails, fix within that task before committing — do not advance.
