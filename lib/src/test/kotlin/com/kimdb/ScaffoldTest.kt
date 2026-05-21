package com.kimdb

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ScaffoldTest {
    @Test
    fun repositoryScaffoldCompiles() {
        val repo = InMemoryKimdbRepository()
        assertTrue(repo.getTitles().none())
        assertTrue(repo.getNames().none())
    }
}