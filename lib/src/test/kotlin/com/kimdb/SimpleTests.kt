package com.kimdb

import com.kimdb.model.Genre
import com.kimdb.model.IsAdult
import com.kimdb.model.NConst
import com.kimdb.model.TConst
import com.kimdb.model.Title
import com.kimdb.model.TitleType
import com.kimdb.tsv.ImdbDatasetManifestGenerator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Year

class SimpleTests {
    @Test
    fun getAirplaneTest() {
        val expected =
            Title(
                tconst = TConst.of("tt0080339"),
                titleType = TitleType.MOVIE,
                primaryTitle = "Airplane!",
                originalTitle = "Airplane!",
                isAdult = IsAdult.NOT_ADULT,
                startYear = Year.of(1980),
                endYear = null,
                runTimeMinutes = 88L,
                genres = setOf(Genre.COMEDY)
            )

        assertEquals(expected, TestImdbFixture.repository.getTitle(TConst.of("tt0080339")))
    }

    @Test
    fun titleCountTest() {
        assertEquals(12_515_073L, TestImdbFixture.resourceDataLineCount(ImdbDatasetManifestGenerator.TITLE_BASICS_TSV))
    }

    @Test
    fun nameCountTest() {
        assertEquals(15_343_396L, TestImdbFixture.resourceDataLineCount(ImdbDatasetManifestGenerator.NAME_BASICS_TSV))
    }

    @Test
    fun primaryTitleToTitles() {
        val actual = TestImdbFixture.repository.getTitlesByPrimaryTitle("The Ladykillers").toSet()
        val expected =
            listOf("tt0048281", "tt0335245", "tt0952211", "tt11188176", "tt29229924")
                .map { TestImdbFixture.repository.getTitle(TConst.of(it)) }
                .toSet()

        assertEquals(expected, actual)
    }

    @Test
    fun primaryNameToNames() {
        val actual = TestImdbFixture.repository.getNamesByPrimaryName("Michelle Williams").toSet()
        val expected =
            listOf(
                "nm0931329", "nm0931330", "nm0931331", "nm0931332", "nm10061157", "nm10198370", "nm10923766",
                "nm11282510", "nm11785960", "nm12034243", "nm12362624", "nm12908310", "nm13503645", "nm13623101",
                "nm13797774", "nm14167942", "nm14173450", "nm14413401", "nm15285139", "nm1578308", "nm15950076",
                "nm16524261", "nm16558194", "nm16776300", "nm16904573", "nm17387657", "nm17569372", "nm17734964",
                "nm18460517", "nm18520093", "nm2031450", "nm2432764", "nm2855931", "nm3002922", "nm3322104", "nm3462771",
                "nm3619775", "nm4605440", "nm4639912", "nm4834939", "nm5108140", "nm5135279", "nm5264512",
                "nm5509862", "nm5549432", "nm5725750", "nm5864656", "nm5906541", "nm6085081", "nm6543169",
                "nm6610391", "nm6977389", "nm7242647", "nm7838196", "nm7974150", "nm8053282", "nm8184108",
                "nm8917329", "nm8939387", "nm9477237", "nm9504971", "nm9603374", "nm9852220"
            ).map { TestImdbFixture.repository.getName(NConst.of(it)) }
                .toSet()

        assertEquals(expected, actual)
    }

    @Test
    fun getTitlesByTypeAndLengthReturnsOnlyMatchingRows() {
        val results = TestImdbFixture.repository.getTitlesByTypeAndLength(TitleType.MOVIE, 10)
        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.titleType == TitleType.MOVIE && it.primaryTitle.length == 10 })
    }
}
