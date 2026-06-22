# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0] - 2026-06-22

### Added

- **Domain models**: `Gender` enum, immutable `Athlete` (id-based equality, `age(asOf)`, serializable), `Result` contract interface.
- **Competition hierarchy**: abstract `Competition` with gender/min-age/duplicate-registration enforcement; `SkiSlalom` (two-run qualification + combined-time ranking) and `Biathlon` (penalty-per-miss final-time ranking, serializable).
- **Olympics aggregate**: `Olympics` collects competitions and computes medalists (`Medal`/`Medalist`), medals per country, average participant age, and the youngest medalist.
- **Exception hierarchy**: `CompetitionException` base with `IneligibleAthleteException` and `DuplicateRegistrationException`.
- **I/O**: `RankingWriter` exports all final rankings to a UTF-8 text file (3-decimal formatting); `BiathlonSerializer` round-trips `Biathlon` via Java serialization.
- **Console UI**: `ConsoleApp` interactive Scanner-driven menu (rankings, medalists, medals by country, statistics, export); `Main` with `--demo` flag for non-interactive demo mode.
- **Demo data**: `DemoData` builds a sample Olympics (8 athletes, 2 competitions, DNF paths exercised).
- **Build tooling**: Gradle 9.5.1 wrapper, JDK 21 toolchain, JaCoCo coverage (XML + HTML), GitHub Actions CI with Codecov upload.
- **Test suite**: 21 JUnit 5 tests across 7 test classes covering all logic-bearing classes (TDD: RED → GREEN → COMMIT).

[Unreleased]: https://github.com/1Git2Clone/citb408-project-winter-olympics/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/1Git2Clone/citb408-project-winter-olympics/releases/tag/v1.0.0