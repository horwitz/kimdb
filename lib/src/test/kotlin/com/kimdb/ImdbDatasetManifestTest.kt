package com.kimdb

import com.kimdb.tsv.ImdbDatasetManifestGenerator
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ImdbDatasetManifestTest {
    @Test
    fun manifestIncludesTsvAndGzFilesWhenPresent() {
        val manifest = ImdbDatasetManifestGenerator.generate(TestImdbFixture.resourcesDir)
        val keys = manifest.files.keys

        assertTrue(ImdbDatasetManifestGenerator.TITLE_BASICS_TSV in keys)
        assertTrue(ImdbDatasetManifestGenerator.TITLE_BASICS_TSV_GZ in keys)
        assertTrue(ImdbDatasetManifestGenerator.NAME_BASICS_TSV in keys)
        assertTrue(ImdbDatasetManifestGenerator.NAME_BASICS_TSV_GZ in keys)

        manifest.files.forEach { (key, file) ->
            assertTrue(file.sizeBytes > 0L, "Expected positive size for $key")
            assertTrue(file.sha256.length == 64, "Expected SHA-256 hex for $key")
        }

        val json = ImdbDatasetManifestGenerator.toJson(manifest)

        assertTrue(json.trimStart().startsWith("{"), "Expected JSON object")
        assertTrue(json.contains("\"files\""), "Expected files field in manifest JSON")
    }
}
