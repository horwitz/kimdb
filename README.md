# kimdb

Utilities and query APIs for IMDb `name.basics` and `title.basics` datasets (from `https://datasets.imdbws.com/`).

## Updating Dataset Files

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

The manifest includes file sizes and SHA-256 checksums for both TSV and GZ files, plus row-count/header metadata for TSV
files.

### Checklist for Updating

1. Replace all four files together in `src/main/resources`:
    - `title.basics.tsv`
    - `title.basics.tsv.gz`
    - `name.basics.tsv`
    - `name.basics.tsv.gz`
2. Regenerate `dataset-manifest.json`:

```bash
./gradlew :lib:generateDatasetManifest
```

3. Rebuild SQLite from the same TSV snapshot:

```bash
./gradlew :lib:importImdbToSqlite
```

4. Verify row-count and index integrity:

```bash
./gradlew :lib:verifyImdbToSqlite
```

5. Run tests against the updated snapshot:

```bash
./gradlew :lib:test
```

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

Verify in-memory vs SQLite backend parity for key queries:

```bash
./gradlew :lib:verifyBackendsParity
```

With explicit paths:

```bash
./gradlew :lib:verifyBackendsParity --args="-r src/main/resources -d build/kimdb.db"
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

### Baseline (2026-05-23)

Command used:

```bash
./gradlew :lib:benchmarkBackends --args="-f csv -o build/benchmarks/backend-benchmark.csv -w 5 -n 20"
```

Observed output (`ns/op`):

| operation                | in_memory_ns |   sqlite_ns | sqlite_over_in_memory_ratio |
|--------------------------|-------------:|------------:|----------------------------:|
| getTitlesByPrimaryTitle  |          565 |   1,493,900 |                   2644.0708 |
| getNamesByPrimaryName    |          380 |   1,516,920 |                   3991.8947 |
| getTitle                 |          910 |     586,025 |                    643.9835 |
| getTitlesByTypeAndLength |          535 | 195,120,565 |                 364711.3364 |

## Using via SQLite

Consumers use SQLite support in three steps:

1. Build a SQLite DB from IMDb TSV files (once per dataset snapshot).
2. Point kimdb at that DB with `KImdb.sqliteRepository(Path)`.
3. Query via `KImdbRepository`.

Example:

```kotlin
import com.kimdb.KImdb
import com.kimdb.model.TConst
import java.nio.file.Path

val repo = KImdb.sqliteRepository(Path.of("path/to/kimdb.db"))
val title = repo.getTitle(TConst.of("tt0080339"))
println(title?.primaryTitle)
```

### From Published Artifact (No kimdb Repo Clone)

The Gradle tasks shown above (`:lib:importImdbToSqlite`, `:lib:verifyImdbToSqlite`, etc.) exist only in the kimdb repo.
If you are using kimdb as a dependency, prefer adding `JavaExec` tasks in your own build so Gradle provides the full
runtime classpath automatically:

```kotlin
tasks.register<JavaExec>("importImdbToSqlite") {
    group = "application"
    description = "Import IMDb TSV data into SQLite database."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "com.kimdb.tsv.ImportImdbToSqliteKt"
    args("-r", "src/main/resources", "-d", "build/kimdb/kimdb.db")
}

tasks.register<JavaExec>("verifyImdbToSqlite") {
    group = "verification"
    description = "Verify SQLite row counts and indexes against IMDb TSV files."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "com.kimdb.tsv.VerifyImdbToSqliteKt"
    args("-r", "src/main/resources", "-d", "build/kimdb/kimdb.db")
}

tasks.register<JavaExec>("verifyBackendsParity") {
    group = "verification"
    description = "Compare key query results between in-memory and SQLite repositories."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "com.kimdb.bench.VerifyBackendsParityKt"
    args("-r", "src/main/resources", "-d", "build/kimdb/kimdb.db")
}
```

Then run:

```bash
./gradlew importImdbToSqlite
./gradlew verifyImdbToSqlite
./gradlew verifyBackendsParity
```

If prefer to run directly with `java -cp`, include your full app runtime classpath (kimdb plus transitive dependencies):

```bash
java -cp "<your_app_classpath_with_kimdb_and_deps>" com.kimdb.tsv.ImportImdbToSqliteKt -r /path/to/resources -d /path/to/kimdb.db
java -cp "<your_app_classpath_with_kimdb_and_deps>" com.kimdb.tsv.VerifyImdbToSqliteKt -r /path/to/resources -d /path/to/kimdb.db
java -cp "<your_app_classpath_with_kimdb_and_deps>" com.kimdb.bench.VerifyBackendsParityKt -r /path/to/resources -d /path/to/kimdb.db
```

The published artifact does not include TSV payloads or a prebuilt SQLite DB, so consumers must provide TSV files and DB
path explicitly. Use a generated location like `build/kimdb/kimdb.db` for the database, not `src/main/resources`.
