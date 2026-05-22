package com.kimdb.tsv

object SqliteSchema {
    object Tables {
        const val TITLES = "titles"
        const val NAMES = "names"
    }

    object Columns {
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

    object Indexes {
        const val TITLES_TCONST = "idx_titles_tconst"
        const val TITLES_PRIMARY_TITLE = "idx_titles_primaryTitle"
        const val TITLES_TYPE_LENGTH = "idx_titles_type_length"
        const val NAMES_NCONST = "idx_names_nconst"
        const val NAMES_PRIMARY_NAME = "idx_names_primaryName"
    }

    val requiredIndexes: Set<String> =
        linkedSetOf(
            Indexes.TITLES_TCONST,
            Indexes.TITLES_PRIMARY_TITLE,
            Indexes.TITLES_TYPE_LENGTH,
            Indexes.NAMES_NCONST,
            Indexes.NAMES_PRIMARY_NAME
        )
}
