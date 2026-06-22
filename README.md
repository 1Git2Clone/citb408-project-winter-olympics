# Winter Olympics Manager

[![GH_Build Icon]][GH_Build Status]&emsp;[![Coverage Icon]][Coverage Status]&emsp;[![License Icon]][LICENSE]

[GH_Build Icon]: https://img.shields.io/github/actions/workflow/status/1Git2Clone/citb408-project-winter-olympics/ci.yml?branch=main
[GH_Build Status]: https://github.com/1Git2Clone/citb408-project-winter-olympics/actions?query=branch%3Amain
[Coverage Icon]: https://codecov.io/gh/1Git2Clone/citb408-project-winter-olympics/branch/main/graph/badge.svg
[Coverage Status]: https://codecov.io/gh/1Git2Clone/citb408-project-winter-olympics
[License Icon]: https://img.shields.io/badge/license-MIT-blue.svg
[License]: LICENSE

<!-- markdownlint-disable MD033 -->
<p>
  <img
    height="50px"
    src="https://codeberg.org/1Kill2Steal/skill-icons/raw/branch/main/icons/Java-Dark.svg"
    alt="Java"
  />
  <img
    height="50px"
    src="https://codeberg.org/1Kill2Steal/skill-icons/raw/branch/main/icons/GithubActions-Dark.svg"
    alt="GitHub Actions"
  />
</p>
<!-- markdownlint-enable MD033 -->

CITB408 (Programming with Java) course project — Spring 2025/2026, NBU.
Variant 2: an application for managing a Winter Olympics with two
competition types, Ski Slalom and Biathlon.

## Overview

- **Athletes**: id, name, country, gender, date of birth.
- **Ski Slalom**: two runs; only the top finishers from run 1 advance to
  run 2; final ranking is by combined time; a DNF in either run excludes
  the athlete from the final ranking.
- **Biathlon**: ski time plus a shooting penalty (fixed time added per
  missed shot); ranking by best final time; a DNF excludes the athlete.
- **Olympics**: aggregates results across competitions, computes medal
  standings, medals per country, average participant age, and the
  youngest medalist; writes final rankings to a text file; Biathlon data
  supports Java serialization/deserialization.

## Architecture

The project follows a layered design under the base package
`org.nbu.citb408.olympics`, separating data models, business logic,
exception handling, I/O, and the console UI:

| Package / type | Responsibility |
|----------------|----------------|
| `model` | Data models: `Gender`, immutable `Athlete` (id-based equality, `age(asOf)`, serializable), and the `Result` contract (`athlete`, `finalTime`, `isDnf`). |
| `competition` | Business logic for events. Abstract `Competition` enforces gender / minimum-age / duplicate-registration rules. `SkiSlalom` (+`SkiSlalomResult`) implements two-run qualification and combined-time ranking; `Biathlon` (+`BiathlonResult`) implements penalty-per-miss final-time ranking and is serializable. |
| `olympics` | Aggregation: `Olympics` collects competitions and computes medalists (`Medal`/`Medalist`), medals per country, average participant age, and the youngest medalist. |
| `exception` | Domain exception hierarchy: `CompetitionException` base, with `IneligibleAthleteException` and `DuplicateRegistrationException`. |
| `io` | `RankingWriter` exports all final rankings to a text file; `BiathlonSerializer` serializes/deserializes `Biathlon` data. |
| `Main` / `ConsoleApp` / `DemoData` | Entry point with a `Scanner`-driven interactive menu and a `--demo` report mode, plus a reusable sample-data builder. |

DNF athletes are excluded from final rankings throughout; times are tracked
in seconds and displayed to three decimal places.

The full task-by-task implementation plan lives in
[`docs/superpowers/plans/2026-06-22-winter-olympics.md`](docs/superpowers/plans/2026-06-22-winter-olympics.md).

## Requirements

- JDK 21 (toolchain-pinned via Gradle; no system-wide Java change needed)
- Gradle (wrapper included — use `./gradlew`, no local install required)

## Build & Run

### Build

```sh
./gradlew build                    # compile + test + jar
./gradlew build jacocoTestReport   # build + coverage report (XML + HTML)
```

### Run

**Demo mode** (non-interactive, prints rankings + medalists to stdout):

```sh
./gradlew run --args="--demo"
```

**Interactive mode** (Scanner-driven menu):

```sh
./gradlew run
```

Then choose from the menu:

```
=== Winter Olympics 2026 ===
1) Show rankings
2) Show medalists
3) Show medals by country
4) Show statistics
5) Export rankings to file
0) Exit
```

Option 5 writes the final rankings to `build/rankings.txt`.

### Test

```sh
./gradlew test               # run all 21 tests
./gradlew test --tests "org.nbu.citb408.olympics.competition.SkiSlalomTest"  # one class
```

Coverage report: `build/reports/jacoco/test/jacocoTestReport/index.html`

Continuous integration runs the build and uploads coverage to Codecov via
GitHub Actions (`.github/workflows/ci.yml`).

## Status

Implemented: domain models, Ski Slalom (two-run combined ranking), Biathlon
(penalty-based ranking, serializable), Olympics aggregate (medals, medals per
country, average age, youngest medalist), text ranking export, Biathlon
serialization, interactive console menu, demo mode (`./gradlew run --args="--demo"`),
and a JUnit 5 test suite with JaCoCo coverage and GitHub Actions CI.

## License

[MIT](LICENSE)

