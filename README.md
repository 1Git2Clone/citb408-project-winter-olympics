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

## Requirements

- JDK 21 (toolchain-pinned via Gradle; no system-wide Java change needed)
- Gradle (wrapper included — use `./gradlew`, no local install required)

## Build & Run

```sh
./gradlew run
./gradlew test
```

## Status

Project scaffolded (Gradle + JDK 21 toolchain, package
`org.nbu.citb408.olympics`); domain model implementation in progress.

## License

[MIT](LICENSE)
