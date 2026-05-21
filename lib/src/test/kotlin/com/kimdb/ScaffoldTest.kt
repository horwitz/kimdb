package com.kimdb

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ScaffoldTest {
    @Test
    fun repositoryScaffoldCompiles() {
        val repo = KImdb.inMemoryRepository()
        assertTrue(repo.getTitles().isEmpty())
        assertTrue(repo.getNames().isEmpty())
    }
}
