package com.kimdb.model

import java.time.Year

data class Title(
    val tconst: TConst,
    val titleType: TitleType,
    val primaryTitle: String,
    val originalTitle: String,
    val isAdult: IsAdult,
    val startYear: Year?,
    val endYear: Year?,
    val runTimeMinutes: Long?,
    val genres: Set<Genre>
)
