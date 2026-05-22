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
