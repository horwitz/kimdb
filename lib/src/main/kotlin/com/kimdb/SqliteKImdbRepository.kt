package com.kimdb

import com.kimdb.api.KImdbRepository
import com.kimdb.model.Genre
import com.kimdb.model.IsAdult
import com.kimdb.model.NConst
import com.kimdb.model.Name
import com.kimdb.model.TConst
import com.kimdb.model.Title
import com.kimdb.model.TitleType
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

class SqliteKImdbRepository(
    dbPath: Path
) : KImdbRepository {
    private object Tables {
        const val TITLES = "titles"
        const val NAMES = "names"
    }

    private object Columns {
        const val TCONST = "tconst"
        const val TITLE_TYPE = "titleType"
        const val PRIMARY_TITLE = "primaryTitle"
        const val ORIGINAL_TITLE = "originalTitle"
        const val IS_ADULT = "isAdult"
        const val START_YEAR = "startYear"
        const val END_YEAR = "endYear"
        const val RUN_TIME_MINUTES = "runTimeMinutes"
        const val GENRES = "genres"
        const val PRIMARY_TITLE_LENGTH = "primaryTitleLength"

        const val NCONST = "nconst"
        const val PRIMARY_NAME = "primaryName"
        const val BIRTH_YEAR = "birthYear"
        const val DEATH_YEAR = "deathYear"
        const val PRIMARY_PROFESSION = "primaryProfession"
        const val KNOWN_FOR_TITLES = "knownForTitles"
    }

    private val titleColumns =
        listOf(
            Columns.TCONST,
            Columns.TITLE_TYPE,
            Columns.PRIMARY_TITLE,
            Columns.ORIGINAL_TITLE,
            Columns.IS_ADULT,
            Columns.START_YEAR,
            Columns.END_YEAR,
            Columns.RUN_TIME_MINUTES,
            Columns.GENRES
        ).joinToString(", ")

    private val nameColumns =
        listOf(
            Columns.NCONST,
            Columns.PRIMARY_NAME,
            Columns.BIRTH_YEAR,
            Columns.DEATH_YEAR,
            Columns.PRIMARY_PROFESSION,
            Columns.KNOWN_FOR_TITLES
        ).joinToString(", ")

    private val jdbcUrl = "jdbc:sqlite:${dbPath.toAbsolutePath()}"

    override fun getTitlesByPrimaryTitle(movieTitleAsString: String) = queryList(
        """
            SELECT $titleColumns
            FROM ${Tables.TITLES}
            WHERE ${Columns.PRIMARY_TITLE} = ?
        """.trimIndent(),
        rowMapper = { toTitle() }
    ) { ps ->
        ps.setString(1, movieTitleAsString)
    }

    override fun getNamesByPrimaryName(nameAsString: String) = queryList(
        """
            SELECT $nameColumns
            FROM ${Tables.NAMES}
            WHERE ${Columns.PRIMARY_NAME} = ?
        """.trimIndent(),
        rowMapper = { toName() }
    ) { ps ->
        ps.setString(1, nameAsString)
    }

    override fun getTitle(id: TConst) = queryList(
        """
            SELECT $titleColumns
            FROM ${Tables.TITLES}
            WHERE ${Columns.TCONST} = ?
        """.trimIndent(),
        rowMapper = { toTitle() }
    ) { ps ->
        ps.setString(1, id.value)
    }.firstOrNull()

    override fun getName(id: NConst) = queryList(
        """
            SELECT $nameColumns
            FROM ${Tables.NAMES}
            WHERE ${Columns.NCONST} = ?
        """.trimIndent(),
        rowMapper = { toName() }
    ) { ps ->
        ps.setString(1, id.value)
    }.firstOrNull()

    override fun getTitlesByTypeAndLength(
        titleType: TitleType,
        length: Int
    ) = queryList(
        """
        SELECT $titleColumns
        FROM ${Tables.TITLES}
        WHERE ${Columns.TITLE_TYPE} = ? AND ${Columns.PRIMARY_TITLE_LENGTH} = ?
        """.trimIndent(),
        rowMapper = { toTitle() }
    ) { ps ->
        ps.setString(1, titleType.imdbValue)
        ps.setInt(2, length)
    }

    override fun getTitles() = queryList(
        """
            SELECT $titleColumns
            FROM ${Tables.TITLES}
        """.trimIndent(),
        rowMapper = { toTitle() }
    ) {}

    override fun getNames() = queryList(
        """
            SELECT $nameColumns
            FROM ${Tables.NAMES}
        """.trimIndent(),
        rowMapper = { toName() }
    ) {}

    private fun <T> queryList(
        sql: String,
        rowMapper: ResultSet.() -> T,
        bind: (java.sql.PreparedStatement) -> Unit
    ): List<T> = withConnection { connection ->
        connection.prepareStatement(sql).use { ps ->
            bind(ps)
            ps.executeQuery().use { rs ->
                val out = mutableListOf<T>()
                while (rs.next()) out += rs.rowMapper()
                out
            }
        }
    }

    private fun <T> withConnection(block: (Connection) -> T) = DriverManager.getConnection(jdbcUrl).use(block)

    private fun ResultSet.toTitle() = Title(
        tconst = TConst.of(getString(Columns.TCONST)),
        titleType = TitleType.of(getString(Columns.TITLE_TYPE)),
        primaryTitle = getString(Columns.PRIMARY_TITLE),
        originalTitle = getString(Columns.ORIGINAL_TITLE),
        isAdult = IsAdult.of(getString(Columns.IS_ADULT)),
        startYear = getNullableInt(Columns.START_YEAR),
        endYear = getNullableInt(Columns.END_YEAR),
        runTimeMinutes = getNullableLong(Columns.RUN_TIME_MINUTES),
        genres = getNullableString(Columns.GENRES).toCsvSet(Genre::of)
    )

    private fun ResultSet.toName() = Name(
        nconst = NConst.of(getString(Columns.NCONST)),
        primaryName = getString(Columns.PRIMARY_NAME),
        birthYear = getNullableInt(Columns.BIRTH_YEAR),
        deathYear = getNullableInt(Columns.DEATH_YEAR),
        primaryProfession = getNullableString(Columns.PRIMARY_PROFESSION).toCsvSet { it },
        knownForTitles = getNullableString(Columns.KNOWN_FOR_TITLES).toCsvSet(TConst::of)
    )

    private fun ResultSet.getNullableInt(column: String): Int? {
        val value = getInt(column)
        return if (wasNull()) null else value
    }

    private fun ResultSet.getNullableLong(column: String): Long? {
        val value = getLong(column)
        return if (wasNull()) null else value
    }

    private fun ResultSet.getNullableString(column: String): String? = getString(column)

    private fun <T> String?.toCsvSet(mapper: (String) -> T) = this
        ?.split(',')
        ?.asSequence()
        ?.filter(String::isNotBlank)
        ?.map(mapper)
        ?.toSet()
        ?: emptySet()
}
