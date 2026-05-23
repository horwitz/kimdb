package com.kimdb

import com.kimdb.api.KImdbRepository
import com.kimdb.model.Genre
import com.kimdb.model.IsAdult
import com.kimdb.model.NConst
import com.kimdb.model.Name
import com.kimdb.model.TConst
import com.kimdb.model.Title
import com.kimdb.model.TitleType
import com.kimdb.tsv.SqliteSchema
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.time.Year

class SqliteKImdbRepository(
    dbPath: Path
) : KImdbRepository {
    private val c = SqliteSchema.Columns

    private val titleColumns =
        listOf(
            c.TCONST,
            c.TITLE_TYPE,
            c.PRIMARY_TITLE,
            c.ORIGINAL_TITLE,
            c.IS_ADULT,
            c.START_YEAR,
            c.END_YEAR,
            c.RUN_TIME_MINUTES,
            c.GENRES
        ).joinToString(", ")

    private val nameColumns =
        listOf(
            c.NCONST,
            c.PRIMARY_NAME,
            c.BIRTH_YEAR,
            c.DEATH_YEAR,
            c.PRIMARY_PROFESSION,
            c.KNOWN_FOR_TITLES
        ).joinToString(", ")

    private val jdbcUrl = "jdbc:sqlite:${dbPath.toAbsolutePath()}"

    override fun getTitlesByPrimaryTitle(movieTitleAsString: String) = queryList(
        """
            SELECT $titleColumns
            FROM ${SqliteSchema.Tables.TITLES}
            WHERE ${c.PRIMARY_TITLE} = ?
        """.trimIndent(),
        rowMapper = { toTitle() }
    ) { ps ->
        ps.setString(1, movieTitleAsString)
    }

    override fun getNamesByPrimaryName(nameAsString: String) = queryList(
        """
            SELECT $nameColumns
            FROM ${SqliteSchema.Tables.NAMES}
            WHERE ${c.PRIMARY_NAME} = ?
        """.trimIndent(),
        rowMapper = { toName() }
    ) { ps ->
        ps.setString(1, nameAsString)
    }

    override fun getTitle(id: TConst) = queryList(
        """
            SELECT $titleColumns
            FROM ${SqliteSchema.Tables.TITLES}
            WHERE ${c.TCONST} = ?
        """.trimIndent(),
        rowMapper = { toTitle() }
    ) { ps ->
        ps.setString(1, id.value)
    }.firstOrNull()

    override fun getName(id: NConst) = queryList(
        """
            SELECT $nameColumns
            FROM ${SqliteSchema.Tables.NAMES}
            WHERE ${c.NCONST} = ?
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
        FROM ${SqliteSchema.Tables.TITLES}
        WHERE ${c.TITLE_TYPE} = ? AND ${c.PRIMARY_TITLE_LENGTH} = ?
        """.trimIndent(),
        rowMapper = { toTitle() }
    ) { ps ->
        ps.setString(1, titleType.imdbValue)
        ps.setInt(2, length)
    }

    override fun getTitles() = queryList(
        """
            SELECT $titleColumns
            FROM ${SqliteSchema.Tables.TITLES}
        """.trimIndent(),
        rowMapper = { toTitle() }
    ) {}

    override fun getNames() = queryList(
        """
            SELECT $nameColumns
            FROM ${SqliteSchema.Tables.NAMES}
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
        tconst = TConst.of(getString(c.TCONST)),
        titleType = TitleType.of(getString(c.TITLE_TYPE)),
        primaryTitle = getString(c.PRIMARY_TITLE),
        originalTitle = getString(c.ORIGINAL_TITLE),
        isAdult = IsAdult.of(getString(c.IS_ADULT)),
        startYear = getNullableYear(c.START_YEAR),
        endYear = getNullableYear(c.END_YEAR),
        runTimeMinutes = getNullableLong(c.RUN_TIME_MINUTES),
        genres = getNullableString(c.GENRES).toCsvSet(Genre::of)
    )

    private fun ResultSet.toName() = Name(
        nconst = NConst.of(getString(c.NCONST)),
        primaryName = getString(c.PRIMARY_NAME),
        birthYear = getNullableYear(c.BIRTH_YEAR),
        deathYear = getNullableYear(c.DEATH_YEAR),
        primaryProfession = getNullableString(c.PRIMARY_PROFESSION).toCsvSet { it },
        knownForTitles = getNullableString(c.KNOWN_FOR_TITLES).toCsvSet(TConst::of)
    )

    private fun ResultSet.getNullableYear(column: String): Year? {
        val value = getInt(column)
        return if (wasNull()) null else Year.of(value)
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
