package com.kimdb

import com.kimdb.tsv.ImdbDatasetManifestGenerator
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Path

class ImdbDatasetManifestTest {
    @Test
    fun manifestIncludesTsvAndGzFilesWhenPresent() {
        val resourcesDir = resolveResourcesDirFromClasspath("/title.basics.tsv")
        val manifest = ImdbDatasetManifestGenerator.generate(resourcesDir)
        val keys = manifest.files.keys

        assertTrue("title.basics.tsv" in keys)
        assertTrue("title.basics.tsv.gz" in keys)
        assertTrue("name.basics.tsv" in keys)
        assertTrue("name.basics.tsv.gz" in keys)

        manifest.files.forEach { (key, file) ->
            assertTrue(file.sizeBytes > 0L, "Expected positive size for $key")
            assertTrue(file.sha256.length == 64, "Expected SHA-256 hex for $key")
        }

        val json = ImdbDatasetManifestGenerator.toJson(manifest)

        assertTrue(json.trimStart().startsWith("{"), "Expected JSON object")
        assertTrue(json.contains("\"files\""), "Expected files field in manifest JSON")
    }

    private fun resolveResourcesDirFromClasspath(resourcePath: String): Path {
        val url =
            this::class.java.getResource(resourcePath)
                ?: error("Missing classpath resource: $resourcePath")

        return Path.of(url.toURI()).parent
    }
}
