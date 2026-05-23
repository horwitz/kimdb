package com.kimdb

import com.kimdb.model.NConst
import com.kimdb.model.TConst
import com.kimdb.model.TitleType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WhoAmI107Test {
    @Test
    fun liamAndOliviaMovies() {
        val expected =
            listOf(
                Triple("tt2449408", listOf("nm2853102"), listOf("nm5326494")),
                Triple("tt3791332", listOf("nm4311572"), listOf("nm5594676")),
                Triple("tt2835532", listOf("nm5706444"), listOf("nm5706458"))
            ).map { (tConstString, liamsList, oliviasList) ->
                Triple(
                    TestImdbFixture.repository.getTitle(TConst.of(tConstString)),
                    liamsList.map { liamNConstString -> requireNotNull(TestImdbFixture.repository.getName(NConst.of(liamNConstString))) },
                    oliviasList.map { oliviaNConstString -> requireNotNull(TestImdbFixture.repository.getName(NConst.of(oliviaNConstString))) }
                )
            }

        val tConstToLiams = tConstToPeopleWithFirstName("LIAM", "actor")
        val tConstToOlivias = tConstToPeopleWithFirstName("OLIVIA", "actress")
        val sharedTConsts = tConstToLiams.keys intersect tConstToOlivias.keys

        val actual =
            sharedTConsts.map { tConst ->
                val title = TestImdbFixture.repository.getTitle(tConst)
                Triple(title, tConstToLiams[tConst], tConstToOlivias[tConst])
            }

        assertEquals(expected, actual)
    }

    @Test
    fun thirdPersonPeople() {
        val expected =
            listOf(
                "nm0032108", "nm0119411", "nm0135394", "nm0194979", "nm0199764", "nm0290549", "nm0546834", "nm0577568",
                "nm0582228", "nm0591777", "nm0764357", "nm10409611", "nm1076624", "nm1224887", "nm1229643", "nm1674566",
                "nm1785054", "nm1795681", "nm1840213", "nm2064182", "nm2316516", "nm2662972", "nm2974500", "nm3510034",
                "nm3569457", "nm3652134", "nm3866229", "nm4384142", "nm4672648", "nm5307742", "nm5358479", "nm6442608",
                "nm6575317", "nm7431027"
            ).map { nConstString ->
                TestImdbFixture.repository.getName(NConst.of(nConstString))
            }

        val thirdPerson =
            requireNotNull(
                TestImdbFixture.repository.getTitles().singleOrNull { title ->
                    title.primaryTitle.uppercase() == "THIRD PERSON" &&
                        title.titleType == TitleType.MOVIE &&
                        title.startYear == 2013
                }
            )

        val actual =
            TestImdbFixture.repository.getNames().filter { name ->
                val professions = name.primaryProfession
                ("actress" in professions || "actor" in professions) &&
                    thirdPerson.tconst in name.knownForTitles
            }

        assertEquals(expected, actual)
    }

    private fun tConstToPeopleWithFirstName(
        firstNameUppercase: String,
        professionLowercase: String
    ): Map<TConst, List<com.kimdb.model.Name>> {
        val year = 2013

        return TestImdbFixture.repository.getNames()
            .asSequence()
            .filter { name ->
                name.primaryName.split(" ").first().uppercase() == firstNameUppercase &&
                    professionLowercase in name.primaryProfession
            }.flatMap { name ->
                name.knownForTitles.asSequence().filter { tConst ->
                    val title = TestImdbFixture.repository.getTitle(tConst)
                    (title?.startYear == year) &&
                        (title.titleType == TitleType.MOVIE || title.titleType == TitleType.TV_MOVIE)
                }.map { tConst -> tConst to name }
            }.groupBy({ it.first }, { it.second })
    }
}
