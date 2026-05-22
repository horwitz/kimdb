package com.kimdb.model

data class Title(
    val tconst: TConst,
    val titleType: TitleType,
    val primaryTitle: String,
    val originalTitle: String,
    val isAdult: IsAdult,
    val startYear: Int?,
    val endYear: Int?,
    val runTimeMinutes: Long?,
    val genres: Set<Genre>
)
