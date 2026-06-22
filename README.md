# Winter Olympics Manager

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

```sh
./gradlew run                 # interactive console menu
./gradlew run --args="--demo" # print a demo report from sample data
./gradlew test                # run the JUnit 5 suite
./gradlew build               # compile, test, and assemble
./gradlew jacocoTestReport    # generate the coverage report
```

Continuous integration runs the build and uploads coverage to Codecov via
GitHub Actions (`.github/workflows/ci.yml`).

## Status

Project scaffolded (Gradle + JDK 21 toolchain, package
`org.nbu.citb408.olympics`) with a complete implementation plan written;
domain model and business logic implementation in progress.

## License

[MIT](LICENSE)
