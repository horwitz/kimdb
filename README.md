# kimdb

Utilities and query APIs for IMDb `name.basics` and `title.basics` datasets (from `https://datasets.imdbws.com/`).

## Refreshing Dataset Files

When new IMDb files are downloaded from `https://datasets.imdbws.com/`:

1. Replace files in `src/main/resources/`:
   - `title.basics.tsv`
   - `title.basics.tsv.gz`
   - `name.basics.tsv`
   - `name.basics.tsv.gz`
2. Regenerate manifest:

```bash
./gradlew :lib:generateDatasetManifest
```

Optional flags:

```bash
./gradlew :lib:generateDatasetManifest --args="-r src/main/resources -o src/main/resources/dataset-manifest.json -t 2026-05-22T14:00:00Z"
```

Where:
- `-r` / `--resources-dir`: input directory for TSV/GZ files
- `-o` / `--output`: output manifest path
- `-t` / `--downloaded-at-utc`: optional original download timestamp (UTC, ISO-8601)

The manifest includes file sizes and SHA-256 checksums for both TSV and GZ files, plus row-count/header metadata for TSV files.

## SQLite Smoke Test

Import TSV data into SQLite:

```bash
./gradlew :lib:importImdbToSqlite
```

With explicit paths/options:

```bash
./gradlew :lib:importImdbToSqlite --args="-r src/main/resources -d build/kimdb.db -b 2000"
```

Verify import row counts and required indexes:

```bash
./gradlew :lib:verifyImdbToSqlite
```

With explicit paths:

```bash
./gradlew :lib:verifyImdbToSqlite --args="-r src/main/resources -d build/kimdb.db"
```

Run backend smoke test/benchmark (in-memory vs SQLite):

```bash
./gradlew :lib:benchmarkBackends
```

With explicit benchmark inputs:

```bash
./gradlew :lib:benchmarkBackends --args="-r src/main/resources -d build/kimdb.db -w 10 -n 50 --sample-title \"The Avengers\" --sample-name \"Michelle Williams\" --sample-tconst tt0110912 --sample-type movie --sample-length 10"
```

CSV output written to a file:

```bash
./gradlew :lib:benchmarkBackends --args="-f csv -o build/benchmarks/backend-benchmark.csv"
```

### Baseline (2026-05-22)

Command used:

```bash
./gradlew :lib:benchmarkBackends --args="-f csv -o build/benchmarks/backend-benchmark.csv -w 5 -n 20"
```

Observed output (`ns/op`):

| operation | in_memory_ns | sqlite_ns | sqlite_over_in_memory_ratio |
|---|---:|---:|---:|
| getTitlesByPrimaryTitle | 505 | 1,020,340 | 2020.4752 |
| getNamesByPrimaryName | 345 | 1,317,440 | 3818.6667 |
| getTitle | 395 | 533,670 | 1351.0633 |
| getTitlesByTypeAndLength | 625 | 175,162,965 | 280260.7440 |
