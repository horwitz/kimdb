# Changelog

## [0.2.0] - 2026-05-23

### Added
- `InputStream`-based loading support for in-memory repository construction:
  - `KImdb.inMemoryRepositoryFromTsv(InputStream, InputStream)`
  - `ImdbTsvParser.loadRepository(InputStream, InputStream)`
  - `ImdbTsvParser.parseTitleBasics(InputStream)`
  - `ImdbTsvParser.parseNameBasics(InputStream)`

## [0.1.0] - 2026-05-23

### Added
- Core `KImdb` API with repository factories:
  - `inMemoryRepository(...)`
  - `inMemoryRepositoryFromTsv(...)`
  - `sqliteRepository(...)`
- In-memory repository implementation (`InMemoryKImdbRepository`) with indexed lookups for:
  - title by `tconst`
  - name by `nconst`
  - titles by `primaryTitle`
  - names by `primaryName`
  - titles by `(titleType, primaryTitle.length)`
- TSV parsing:
  - `ImdbTsvSupport`
  - `ImdbTsvParser`
  - `ImdbTsvTokenValidation`
- Dataset manifest support:
  - `ImdbDatasetManifest` model and JSON serialization
  - `generateDatasetManifest` Gradle task / CLI entrypoint
- SQLite import and verification tooling:
  - `importImdbToSqlite` Gradle task / CLI entrypoint
  - `verifyImdbToSqlite` Gradle task / CLI entrypoint (row-count and index checks)
- Backend comparison tools:
  - `benchmarkBackends` Gradle task / CLI entrypoint
  - `verifyBackendsParity` Gradle task / CLI entrypoint
- Test suite foundation and parity coverage
- Documentation
  - Added project README
  - Added dataset-refresh checklist and command workflow
  - Documented SQLite smoke-test flow and backend parity command usage
  - Documented benchmark command usage and baseline output
