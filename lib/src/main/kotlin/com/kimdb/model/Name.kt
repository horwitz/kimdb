package com.kimdb.model

import java.time.Year

data class Name(
    val nconst: NConst,
    val primaryName: String,
    val birthYear: Year?,
    val deathYear: Year?,
    val primaryProfession: Set<String>,
    val knownForTitles: Set<TConst>
)
