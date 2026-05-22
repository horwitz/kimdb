package com.kimdb.tsv

import java.nio.file.Path
import kotlin.io.path.useLines

internal const val NULL_TOKEN = "\\N"

internal fun forEachTsvRow(
    path: Path,
    expectedColumns: Int,
    rowConsumer: (List<String>) -> Unit
) {
    path.useLines { lines -> forEachTsvRow(lines, expectedColumns, rowConsumer) }
}

internal fun forEachTsvRow(
    lines: Sequence<String>,
    expectedColumns: Int,
    rowConsumer: (List<String>) -> Unit
) {
    lines
        .drop(1)
        .filter(String::isNotBlank)
        .forEach { line ->
            val cols = line.split('\t')
            require(cols.size == expectedColumns) {
                "Expected $expectedColumns columns, got ${cols.size}: $line"
            }
            rowConsumer(cols)
        }
}

internal fun <T> mapTsvRows(
    path: Path,
    expectedColumns: Int,
    rowMapper: (List<String>) -> T
): List<T> {
    val out = mutableListOf<T>()
    forEachTsvRow(path, expectedColumns) { out += rowMapper(it) }
    return out
}

internal fun String.toCsvTokensOrEmpty(): List<String> = if (this == NULL_TOKEN) {
    emptyList()
} else {
    split(',').filter(String::isNotBlank)
}
