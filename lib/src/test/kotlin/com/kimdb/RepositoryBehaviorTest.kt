package com.kimdb

import com.kimdb.model.NConst
import com.kimdb.model.TConst
import com.kimdb.model.TitleType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RepositoryBehaviorTest {
    @Test
    fun getTitleByIdReturnsExpectedKnownMovie() {
        val title = requireNotNull(TestImdbFixture.repository.getTitle(TConst.of("tt0080339")))
        assertEquals("Airplane!", title.primaryTitle)
    }

    @Test
    fun getNamesByPrimaryNameContainsExpectedKnownId() {
        val names = TestImdbFixture.repository.getNamesByPrimaryName("Michelle Williams")
        assertTrue(names.isNotEmpty())
        assertTrue(names.any { it.nconst == NConst.of("nm0931329") })
    }

    @Test
    fun getTitlesByTypeAndLengthReturnsOnlyMatchingRows() {
        val results = TestImdbFixture.repository.getTitlesByTypeAndLength(TitleType.MOVIE, 10)
        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.titleType == TitleType.MOVIE && it.primaryTitle.length == 10 })
    }
}
